/*
 * Copyright (c) 2019 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.codavaj.process.docparser.DocParser;
import org.codavaj.process.srcwriter.WriterUtils;
import org.codavaj.type.Type;
import org.codavaj.type.TypeFactory;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;


/**
 * Test01.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2019/05/10 umjammer initial version <br>
 */
public class Test01 {

    /**
     * Derive a reflection-like API from a javadoc source tree. Resolve any type names
     * to external javadoc links. External links to Sun's JDK javadoc apis are
     * automatically resolved ( i.e. http://java.sun.com/j2se/X/docs/api/ )
     *
     * @param javadocdir the javadoc tree root
     * @param externaLinks a list of 'http://..' strings representing external javadoc refs.
     *
     * @return a TypeFactory handle on the resulting api
     * @throws Exception any problem.
     */
    public TypeFactory analyze(String javadocdir, List<String> externalLinks) throws Exception {
        DocParser dp = new DocParser();
        dp.setJavadocDirName(javadocdir);
        dp.setExternalLinks(externalLinks);
        dp.addProgressListener(System.err::println);

        return dp.process();
    }

    /**
     * 
     * @param args
     */
    public static void main(String[] args) throws Exception {

        String javadocDir = args[0];
        String externalLink = args[1];
        String sourceDir = args[2];
        String outputDir = args[3];

        List<String> el = new ArrayList<>();
        el.add(externalLink);

        Test01 app = new Test01();
        TypeFactory tf = app.analyze(javadocDir, el);

        for (Type type : tf.getTypes()) {

            Path source = Paths.get(sourceDir, type.getSourceFilename());
            if (!Files.exists(source)) {
                WriterUtils.print(type, Files.newBufferedWriter(source));
System.err.println("C: " + source);
                continue;
            }

            CompilationUnit unit = StaticJavaParser.parse(source);

            unit.accept(new VoidVisitorAdapter<Void>() {
                @Override
                public void visit(ClassOrInterfaceDeclaration n, Void arg) {
//                    System.out.println("----");
//                    System.out.println("CLASS: " + n.getNameAsString());
//                    n.getComment().ifPresent(v -> {
//                        System.out.println("OLD:");
//                        System.out.println(v);
//                    });

                    type.getType(n.getNameAsString()).ifPresent(t -> {
                        t.getCommentAsString().ifPresent(s -> {
//                            System.out.println("--");
//                            System.out.println("NEW:");
//                            System.out.println(s);

                            n.setComment(new JavadocComment(t.getInnerCommentAsString().get()));
                        });
                    });
                    super.visit(n, arg);
                }

                @Override
                public void visit(FieldDeclaration n, Void arg) {
                    for (VariableDeclarator v : n.getVariables()) {
//                        System.out.println("----");
//                        System.out.println("FIELD: " + v.getNameAsString());
//                        v.getComment().ifPresent(w -> {
//                            System.out.println("OLD:");
//                            System.out.println(w);
//                        });

                        type.getType(ClassOrInterfaceDeclaration.class.cast(n.getParentNode().get()).getNameAsString()).ifPresent(t -> {
                            t.getField(v.getNameAsString()).ifPresent(f -> {
                                f.getCommentAsString().ifPresent(s -> {
//                                    System.out.println("--");
//                                    System.out.println("NEW:");
//                                    System.out.println(s);

                                    n.setComment(new JavadocComment(f.getInnerCommentAsString().get()));
                                });
                            });
                        });
                    }

                    super.visit(n, arg);
                }

                @Override
                public void visit(MethodDeclaration n, Void arg) {
//                    System.out.println("----");
//                    System.out.println("METHOD: " + n.getDeclarationAsString());
//                    n.getComment().ifPresent(v -> {
//                        System.out.println("OLD:");
//                        System.out.println(v);
//                    });

                    type.getType(ClassOrInterfaceDeclaration.class.cast(n.getParentNode().get()).getNameAsString()).ifPresent(t -> {

                        t.getMethod(getSignatureString(n)).ifPresent(m -> {
                            m.getCommentAsString().ifPresent(s -> {
//                                System.out.println("--");
//                                System.out.println("NEW:");
//                                System.out.println(s);

                                n.setComment(new JavadocComment(m.getInnerCommentAsString().get()));
                            });
                        });
                    });

                    super.visit(n, arg);
                }

                /** */
                String getSignatureString(MethodDeclaration n) {
                    StringBuilder sb = new StringBuilder(n.getNameAsString());
                    sb.append("(");
                    n.getParameters().forEach(p -> {
                        sb.append(Type.getSignatureString(p.getTypeAsString()));
                    });
                    sb.append(")");
                    sb.append(Type.getSignatureString(n.getTypeAsString()));
                    return sb.toString();
                }

            }, null);

            Path result = Paths.get(outputDir, type.getSourceFilename());
System.err.println("R: " + result);
            Files.write(result, unit.toString().getBytes());
        }
    }
}

/* */
