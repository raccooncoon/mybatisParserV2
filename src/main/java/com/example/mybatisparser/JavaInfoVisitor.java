package com.example.mybatisparser;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithName;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class JavaInfoVisitor extends VoidVisitorAdapter<Void> {

    @Override
    public void visit(ClassOrInterfaceDeclaration n, Void arg) {
        super.visit(n, arg);

        // 패키지 이름 출력
        String packageName = n.findCompilationUnit()
                .flatMap(CompilationUnit::getPackageDeclaration)
                .map(NodeWithName::getNameAsString)
                .orElse("No package");
        log.info("packageName : {}", packageName);

        // 클래스 이름 출력
        String className = n.getNameAsString();
        log.info("className : {}", className);

        // 클래스 어노테이션 리스트 출력
        List<String> classAnnotationList = n.getAnnotations().stream().map(Node::toString).toList();
        log.info("classAnnotationList : {}", classAnnotationList);

        // 클래스 필드 리스트 출력
        List<FieldDeclaration> fields = n.getFields();
        log.info("fields : {}", fields);

        n.getMethods().forEach(method -> {

            // 메서드 이름 출력
            List<String> list = method.getAnnotations().stream().map(NodeWithName::getNameAsString).toList();
            log.info("list : {}", list);

            // 메서드 어노테이션 리스트 출력
            String methodName = method.getNameAsString();
            log.info("methodName : {}", methodName);

            // 메서드 내용을 추출 하고 출력
            String methodContent = method.getBody().map(Node::toString).orElse("No content");
            log.info("methodContent : {}", methodContent);

        });
    }
}
