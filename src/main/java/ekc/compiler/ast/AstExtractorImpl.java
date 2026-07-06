package ekc.compiler.ast;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.RecordDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import ekc.shared.model.acquisition.CompilerContext;
import ekc.shared.model.acquisition.RepositoryMetadata;
import ekc.shared.model.ast.*;
import ekc.shared.model.source.RepositorySource;
import ekc.shared.model.structure.ExtractedSourceFile;
import ekc.shared.model.structure.RepositoryStructure;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AstExtractorImpl implements AstExtractor {

    @Override
    public CompilerContext extract(CompilerContext context) {

        RepositoryMetadata metadata =
                context.getRepositoryMetadata();

        RepositorySource repositorySource =
                context.getRepositorySource();

        RepositoryAst repositoryAst =
                context.getRepositoryAst();

        List<ExtractedSourceFile> extractedSourceFiles =
                new ArrayList<>();

        for (ParsedSourceFile parsedSourceFile
                : repositoryAst.getParsedSourceFiles()) {

            ParsedCompilationUnit parsedCompilationUnit =
                    extractCompilationUnit(
                            parsedSourceFile.getCompilationUnit());

            extractedSourceFiles.add(
                    new ExtractedSourceFile(
                            parsedSourceFile,
                            parsedCompilationUnit
                    )
            );
        }

        RepositoryStructure repositoryStructure =
                new RepositoryStructure(extractedSourceFiles);

        System.out.println();
        System.out.println("========== AST EXTRACTION REPORT ==========");
        System.out.println();

        for (ExtractedSourceFile file : extractedSourceFiles) {

            ParsedCompilationUnit unit =
                    file.getParsedCompilationUnit();

            System.out.println("-----------------------------------------");

            System.out.println(
                    file.getParsedSourceFile()
                            .getSourceFile()
                            .getRelativePath());

            System.out.println("Package : "
                    + unit.getPackageName());

            System.out.println("Imports : "
                    + unit.getImports().size());

            System.out.println("Types   : "
                    + unit.getTypes().size());

            for (ParsedType type : unit.getTypes()) {

                System.out.println();

                System.out.println("Type : "
                        + type.getName());

                System.out.println("Kind : "
                        + type.getKind());

                System.out.println("Fields : "
                        + type.getFields().size());

                System.out.println("Methods : "
                        + type.getMethods().size());

                System.out.println("Annotations : "
                        + type.getAnnotations().size());
            }
        }

        System.out.println();
        System.out.println("===========================================");

        return new CompilerContext(
                metadata,
                repositorySource,
                repositoryAst,
                repositoryStructure
        );
    }

    /**
     * Extracts compiler model from a CompilationUnit.
     */
    private ParsedCompilationUnit extractCompilationUnit(
            CompilationUnit compilationUnit) {

        String packageName =
                compilationUnit.getPackageDeclaration()
                        .map(packageDeclaration ->
                                packageDeclaration.getNameAsString())
                        .orElse(null);

        List<String> imports =
                compilationUnit.getImports()
                        .stream()
                        .map(ImportDeclaration::getNameAsString)
                        .toList();

        List<ParsedType> types =
                compilationUnit.getTypes()
                        .stream()
                        .map(this::extractType)
                        .toList();

        return new ParsedCompilationUnit(
                packageName,
                imports,
                types
        );
    }

    /**
     * Extracts a ParsedType.
     */
    private ParsedType extractType(
            TypeDeclaration<?> declaration) {

        TypeKind typeKind;

        if (declaration instanceof ClassOrInterfaceDeclaration classDeclaration) {

            typeKind = classDeclaration.isInterface()
                    ? TypeKind.INTERFACE
                    : TypeKind.CLASS;

        } else if (declaration instanceof EnumDeclaration) {

            typeKind = TypeKind.ENUM;

        } else if (declaration instanceof RecordDeclaration) {

            typeKind = TypeKind.RECORD;

        } else {

            typeKind = TypeKind.ANNOTATION;
        }

        List<String> modifiers =
                declaration.getModifiers()
                        .stream()
                        .map(modifier -> modifier.getKeyword().asString())
                        .toList();

        List<String> annotations =
                declaration.getAnnotations()
                        .stream()
                        .map(annotation -> annotation.getNameAsString())
                        .toList();

        List<ParsedMethod> methods =
                declaration.getMethods()
                        .stream()
                        .map(this::extractMethod)
                        .toList();

        List<ParsedField> fields =
                declaration.getFields()
                        .stream()
                        .flatMap(field ->
                                field.getVariables()
                                        .stream()
                                        .map(variable ->
                                                extractField(
                                                        field,
                                                        variable.getNameAsString())))
                        .toList();

        return new ParsedType(
                declaration.getNameAsString(),
                typeKind,
                modifiers,
                annotations,
                methods,
                fields
        );
    }

    /**
     * Extracts a ParsedMethod.
     */
    private ParsedMethod extractMethod(
            MethodDeclaration method) {

        List<String> modifiers =
                method.getModifiers()
                        .stream()
                        .map(modifier ->
                                modifier.getKeyword().asString())
                        .toList();

        List<String> annotations =
                method.getAnnotations()
                        .stream()
                        .map(annotation ->
                                annotation.getNameAsString())
                        .toList();

        List<String> parameterTypes =
                method.getParameters()
                        .stream()
                        .map(parameter ->
                                parameter.getType().asString())
                        .toList();

        return new ParsedMethod(
                method.getNameAsString(),
                method.getType().asString(),
                modifiers,
                annotations,
                parameterTypes
        );
    }

    /**
     * Extracts a ParsedField.
     */
    private ParsedField extractField(
            FieldDeclaration field,
            String fieldName) {

        List<String> modifiers =
                field.getModifiers()
                        .stream()
                        .map(modifier ->
                                modifier.getKeyword().asString())
                        .toList();

        List<String> annotations =
                field.getAnnotations()
                        .stream()
                        .map(annotation ->
                                annotation.getNameAsString())
                        .toList();

        return new ParsedField(
                fieldName,
                field.getElementType().asString(),
                modifiers,
                annotations
        );
    }
}