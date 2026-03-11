package io.promptspecj.codegen.java;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import io.promptspecj.model.OutputSpec;
import io.promptspecj.model.PromptSpecDefinition;
import io.promptspecj.model.VariableSpec;
import io.promptspecj.runtime.PromptContract;
import io.promptspecj.validator.PromptLockfileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.Modifier;

public final class JavaPromptSpecGenerator {
    public void generate(GenerationRequest request) throws IOException {
        Files.createDirectories(request.outputDirectory());
        for (PromptSpecDefinition prompt : request.document().prompts()) {
            generateInputs(prompt, request);
            generateMetadata(prompt, request);
            generateInterface(prompt, request);
            generateClient(prompt, request);
        }
    }

    private void generateInputs(PromptSpecDefinition prompt, GenerationRequest request) throws IOException {
        TypeSpec.Builder builder = TypeSpec.classBuilder(inputsClassName(prompt))
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);
        MethodSpec.Builder constructor = MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC);
        for (VariableSpec variable : prompt.variables()) {
            TypeName type = javaType(variable);
            builder.addField(FieldSpec.builder(type, variable.name(), Modifier.PRIVATE, Modifier.FINAL).build());
            constructor.addParameter(type, variable.name())
                    .addStatement("this.$N = $N", variable.name(), variable.name());
            builder.addMethod(MethodSpec.methodBuilder(variable.name())
                    .addModifiers(Modifier.PUBLIC)
                    .returns(type)
                    .addStatement("return $N", variable.name())
                    .build());
        }
        builder.addMethod(constructor.build());
        JavaFile.builder(request.basePackage(), builder.build()).build().writeTo(request.outputDirectory());
    }

    private void generateMetadata(PromptSpecDefinition prompt, GenerationRequest request) throws IOException {
        TypeSpec type = TypeSpec.classBuilder(metadataClassName(prompt))
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addField(constant("PROMPT_ID", prompt.id()))
                .addField(constant("PROMPT_VERSION", prompt.version()))
                .addField(constant("TEMPLATE_HASH", PromptLockfileWriter.hash(prompt.template().value())))
                .addField(constant(
                        "OUTPUT_SCHEMA_HASH",
                        PromptLockfileWriter.hash(outputSignature(prompt.output()))))
                .addMethod(MethodSpec.constructorBuilder().addModifiers(Modifier.PRIVATE).build())
                .build();
        JavaFile.builder(request.basePackage(), type).build().writeTo(request.outputDirectory());
    }

    private void generateInterface(PromptSpecDefinition prompt, GenerationRequest request) throws IOException {
        TypeSpec type = TypeSpec.interfaceBuilder(interfaceClassName(prompt))
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(ParameterizedTypeName.get(
                        ClassName.get(PromptContract.class),
                        ClassName.get(request.basePackage(), inputsClassName(prompt)),
                        outputTypeName(request.basePackage(), prompt.output())))
                .addMethod(MethodSpec.methodBuilder("execute")
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .returns(outputTypeName(request.basePackage(), prompt.output()))
                        .addParameter(ClassName.get(request.basePackage(), inputsClassName(prompt)), "input")
                        .build())
                .build();
        JavaFile.builder(request.basePackage(), type).build().writeTo(request.outputDirectory());
    }

    private void generateClient(PromptSpecDefinition prompt, GenerationRequest request) throws IOException {
        ClassName inputsType = ClassName.get(request.basePackage(), inputsClassName(prompt));
        ClassName metadataType = ClassName.get(request.basePackage(), metadataClassName(prompt));
        ClassName descriptorType = ClassName.get("io.promptspecj.springai", "SpringAiPromptDescriptor");
        ClassName executorType = ClassName.get("io.promptspecj.springai", "SpringAiPromptExecutor");
        ClassName toolRegistryType = ClassName.get("io.promptspecj.runtime", "ToolRegistry");
        ClassName toolCallbackType = ClassName.get("org.springframework.ai.tool", "ToolCallback");

        TypeSpec type = TypeSpec.classBuilder(clientClassName(prompt))
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addSuperinterface(ClassName.get(request.basePackage(), interfaceClassName(prompt)))
                .addField(FieldSpec.builder(executorType, "executor", Modifier.PRIVATE, Modifier.FINAL).build())
                .addField(FieldSpec.builder(
                                ParameterizedTypeName.get(toolRegistryType, toolCallbackType),
                                "toolRegistry",
                                Modifier.PRIVATE,
                                Modifier.FINAL)
                        .build())
                .addField(FieldSpec.builder(
                                ParameterizedTypeName.get(
                                        descriptorType,
                                        inputsType,
                                        outputTypeName(request.basePackage(), prompt.output())),
                                "descriptor",
                                Modifier.PRIVATE,
                                Modifier.FINAL)
                        .initializer(descriptorInitializer(prompt, request.basePackage()))
                        .build())
                .addMethod(MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(executorType, "executor")
                        .addParameter(ParameterizedTypeName.get(toolRegistryType, toolCallbackType), "toolRegistry")
                        .addStatement("this.executor = executor")
                        .addStatement("this.toolRegistry = toolRegistry")
                        .build())
                .addMethod(MethodSpec.methodBuilder("execute")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(outputTypeName(request.basePackage(), prompt.output()))
                        .addParameter(inputsType, "input")
                        .addStatement("return executor.execute(descriptor, input, toolRegistry)")
                        .build())
                .addMethod(MethodSpec.methodBuilder("metadata")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(ClassName.get("io.promptspecj.runtime", "PromptExecutionMetadata"))
                        .addStatement(
                                "return new $T($T.PROMPT_ID, $T.PROMPT_VERSION, $T.TEMPLATE_HASH, $T.OUTPUT_SCHEMA_HASH)",
                                ClassName.get("io.promptspecj.runtime", "PromptExecutionMetadata"),
                                metadataType,
                                metadataType,
                                metadataType,
                                metadataType)
                        .build())
                .build();
        JavaFile.builder(request.basePackage(), type).build().writeTo(request.outputDirectory());
    }

    private CodeBlock descriptorInitializer(PromptSpecDefinition prompt, String basePackage) {
        Map<String, String> accessors = new LinkedHashMap<>();
        for (VariableSpec variable : prompt.variables()) {
            accessors.put(variable.name(), "input." + variable.name() + "()");
        }
        CodeBlock.Builder mapperBlock = CodeBlock.builder().add("input -> new $T<>() {{\n", LinkedHashMap.class);
        for (Map.Entry<String, String> entry : accessors.entrySet()) {
            mapperBlock.addStatement("put($S, $L)", entry.getKey(), entry.getValue());
        }
        mapperBlock.add("}}");

        return CodeBlock.of(
                "new $T<>($S, $S, $S, $S, $T.class, $L, $L, $L)",
                ClassName.get("io.promptspecj.springai", "SpringAiPromptDescriptor"),
                prompt.id(),
                prompt.version(),
                prompt.template().inline(),
                prompt.output().mode(),
                outputTypeName(basePackage, prompt.output()),
                prompt.tools() == null
                        ? CodeBlock.of("$T.of()", List.class)
                        : CodeBlock.of(
                                "$T.of($L)",
                                List.class,
                                prompt.tools().stream()
                                        .map(tool -> CodeBlock.of("$S", tool.name()))
                                        .reduce((left, right) -> CodeBlock.of("$L, $L", left, right))
                                        .orElse(CodeBlock.of(""))),
                prompt.output().requiresStructuredOutput(),
                mapperBlock.build());
    }

    private FieldSpec constant(String name, String value) {
        return FieldSpec.builder(String.class, name, Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("$S", value == null ? "" : value)
                .build();
    }

    private String outputSignature(OutputSpec outputSpec) {
        if ("java-type".equals(outputSpec.mode())) {
            return outputSpec.javaType();
        }
        return outputSpec.jsonSchema() == null ? outputSpec.mode() : outputSpec.jsonSchema().toPrettyString();
    }

    private String inputsClassName(PromptSpecDefinition prompt) {
        return toTypeName(prompt.id()) + "PromptInputs";
    }

    private String interfaceClassName(PromptSpecDefinition prompt) {
        return toTypeName(prompt.id()) + "Prompt";
    }

    private String clientClassName(PromptSpecDefinition prompt) {
        return toTypeName(prompt.id()) + "PromptClient";
    }

    private String metadataClassName(PromptSpecDefinition prompt) {
        return toTypeName(prompt.id()) + "PromptMetadata";
    }

    private String toTypeName(String promptId) {
        String[] tokens = promptId.split("[._-]");
        StringBuilder builder = new StringBuilder();
        for (String token : tokens) {
            if (token.isBlank()) {
                continue;
            }
            builder.append(Character.toUpperCase(token.charAt(0))).append(token.substring(1));
        }
        return builder.toString();
    }

    private TypeName javaType(VariableSpec variableSpec) {
        return switch (variableSpec.type()) {
            case "int" -> com.squareup.javapoet.TypeName.INT.box();
            case "long" -> com.squareup.javapoet.TypeName.LONG.box();
            case "boolean" -> com.squareup.javapoet.TypeName.BOOLEAN.box();
            case "number" -> ClassName.get(Double.class);
            default -> ClassName.get(String.class);
        };
    }

    private TypeName outputTypeName(String basePackage, OutputSpec outputSpec) {
        if ("java-type".equals(outputSpec.mode()) && outputSpec.javaType() != null) {
            String className = outputSpec.javaType();
            int lastDot = className.lastIndexOf('.');
            return lastDot > 0
                    ? ClassName.get(className.substring(0, lastDot), className.substring(lastDot + 1))
                    : ClassName.get(basePackage, className);
        }
        if ("json-schema".equals(outputSpec.mode())) {
            return ParameterizedTypeName.get(ClassName.get(Map.class), ClassName.get(String.class), ClassName.get(Object.class));
        }
        return ClassName.get(String.class);
    }
}
