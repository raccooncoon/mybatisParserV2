package com.example.mybatisparser;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithName;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

@Slf4j
@AllArgsConstructor
public class JavaInfoVisitor extends VoidVisitorAdapter<Void> {

    private final JavaInfoRepository javaInfoRepository;
    private final ExternalConfig externalConfig;
    private final Path path;

    @Override
    public void visit(ClassOrInterfaceDeclaration n, Void arg) {
        super.visit(n, arg);

        // 패키지 이름 출력
        String packageName = n.findCompilationUnit()
                .flatMap(CompilationUnit::getPackageDeclaration)
                .map(NodeWithName::getNameAsString)
                .orElse("No package");
        //log.info("packageName : {}", packageName);

        // 클래스 이름 출력
        String className = n.getNameAsString();
        //log.info("className : {}", className);

        // 클래스 어노테이션 리스트 출력
        List<String> classAnnotationList = n.getAnnotations().stream().map(Node::toString).toList();
        //log.info("classAnnotationList : {}", classAnnotationList);

        // 클래스 필드 네임 리스트 출력
        List<String> fieldNames = n.getFields().stream()
                .flatMap(field -> field.getVariables().stream()
                        .map(variable -> {
                            String fieldName = variable.getNameAsString();
                            String fieldTypeName = variable.getTypeAsString();
                            return fieldTypeName + " (" + fieldName + ")";
                        }))
                .toList();
        //log.info("fieldNames : {}", fieldNames);

        n.getMethods().forEach(method -> {

            // 서비스 이름
            String serviceName = getServicesName(path.toString());
            //log.info("serviceName : {}", serviceName);

            // 메서드 이름 출력
            String methodName = method.getNameAsString();
            //log.info("methodName : {}", methodName);

            // 메서드 어노테이션 리스트 출력
            List<String> methodAnnotations = method.getAnnotations().stream().map(Node::toString).toList();
            //log.info("methodAnnotations : {}", methodAnnotations);

            // 메서드 호출 리스트 출력
            List<String> methodCalls = method.findAll(MethodCallExpr.class).stream().map(methodCallExpr -> methodCallExpr.getName().toString()).toList();
            //log.info("methodCalls : {}", methodCalls);

            JavaInfoEntity javaInfoEntity = new JavaInfoEntity().builder()
                    .serviceName(serviceName)
                    .filePath(path.toString())
                    .fileName(path.getFileName().toString())
                    .packageName(packageName)
                    .className(className)
                    .classAnnotations(listToJoin(classAnnotationList))
                    .methodName(methodName)
                    .methodCalls(listToJoin(methodCalls))
                    .methodAnnotations(listToJoin(methodAnnotations))
                    .classFields(listToJoin(fieldNames))
                    .build();

            javaInfoRepository.save(javaInfoEntity);

            //log.info("javaInfoEntity : {}", javaInfoEntity);
        });
    }
    private String getServicesName(String filePath) {

        if (Objects.equals(filePath, externalConfig.getFolderPath())) {
            return "File Test";
        }

        String stringWithoutDotSlash = filePath.substring(externalConfig.getFolderPath().length() + 1); // 2는 "./"의 길이

        // 다음 '/'까지의 부분 문자열 가져오기
        int endIndex = stringWithoutDotSlash.indexOf("/");
        if (endIndex != -1) {
            return stringWithoutDotSlash.substring(0, endIndex);
        } else {
            System.out.println("No '/' found in the string after './'.");
        }
        return "";
    }

        public String listToJoin(List<String> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }
        return String.join(",", attribute);
    }
}
