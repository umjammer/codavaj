/*
 * Copyright (c) 2019 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package commentator;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.codavaj.process.docparser.DocParser;
import org.codavaj.type.Type;
import org.codavaj.type.TypeFactory;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.utils.Pair;
import com.google.common.collect.Streams;
import vavi.util.Debug;


/**
 * commentator using java parser (replace comment, refactoring parameter name: failed)
 *
 * <li> java parser doesn't handle fully qualified name
 * <li> refactoring is not easy
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2019/05/10 umjammer initial version <br>
 */
public class JavaParserCommentator {

    /** */
    public TypeFactory analyze(String javadocdir, List<String> externalLinks) throws Exception {
Debug.println("analyze start: " + packageFilter);
        DocParser dp = new DocParser();
        dp.setJavadocClassName(packageFilter + "(\\.[\\w]+)*\\.[A-Z]\\w+$");
        dp.setJavadocDirName(javadocdir);
        dp.setExternalLinks(externalLinks);
        dp.addProgressListener(System.err::println);

        return dp.process();
    }

    /** regex */
    String packageFilter;

    /**
     *
     * @param args 0: javadocDir, 1: externalLink, 2: sourceDir, 3: outputDir
     */
    public static void main(String[] args) throws Exception {
Debug.println("JavaParserCommentator: " + args[0]);
        JavaParserCommentator app = new JavaParserCommentator();
        app.packageFilter = args[4];
        app.exec(args[0], args[1], args[2], args[3]);
    }

    /** */
    void exec(String javadocDir, String externalLink, String sourceDir, String outputDir) throws Exception {
        List<String> el;
        if (externalLink != null && !externalLink.isEmpty()) {
            el = new ArrayList<>();
            el.add(externalLink);
        } else {
            el = Collections.emptyList();
        }

        TypeFactory tf = analyze(javadocDir, el);

        for (Type type : tf.getTypes()) {

            Path source = Paths.get(sourceDir, type.getSourceFilename());
            if (!Files.exists(source)) {
//                WriterUtils.print(type, Files.newBufferedWriter(source));
System.err.println("SK: " + source);
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

                    type.getType(n.getNameAsString()).ifPresent(t -> t.getCommentAsString().ifPresent(s -> {
//                            System.out.println("--");
//                            System.out.println("NEW:");
//                            System.out.println(s);

                        n.setComment(new JavadocComment(t.getInnerCommentAsString().get()));
System.err.println("RC: " + "CLASS: " + n.getNameAsString());
                    }));
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

                        if (n.getParentNode().get() instanceof ClassOrInterfaceDeclaration) {
                            type.getType(((ClassOrInterfaceDeclaration) n.getParentNode().get()).getNameAsString()).flatMap(t -> t.getField(v.getNameAsString())).ifPresent(f -> f.getCommentAsString().ifPresent(s -> {
//                                    System.out.println("--");
//                                    System.out.println("NEW:");
//                                    System.out.println(s);

                                n.setComment(new JavadocComment(f.getInnerCommentAsString().get()));
                                System.err.println("RC: " + "FIELD: " + v.getNameAsString());
                            }));
                        } else {
System.err.println("IG: " + "FIELD: " + v.getNameAsString());
                        }
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

                    if (n.getParentNode().get() instanceof ClassOrInterfaceDeclaration) {
                        type.getType(((ClassOrInterfaceDeclaration) n.getParentNode().get()).getNameAsString()).flatMap(t -> t.getMethod(getSignatureString(n))).ifPresent(m -> {
                            m.getCommentAsString().ifPresent(s -> {
//                                System.out.println("--");
//                                System.out.println("NEW:");
//                                System.out.println(s);

                                n.setComment(new JavadocComment(m.getInnerCommentAsString().get()));
                                System.err.println("RC: " + "METHOD: " + getSignatureString(n));
                            });

                            // fix parameter names (only names at declaration)
                            Streams.zip(m.getParameterList().stream(), n.getParameters().stream(),
                                    Pair::new
                            ).filter(p -> !p.a.getName().equals(p.b.getNameAsString())).forEach(p -> {
                                System.err.println("RN: " + "PARAM: " + p.b.getNameAsString() + " -> " + p.a.getName());
//                                    p.b.setName(p.a.getName()); // TODO currently OFF
                            });
                        });
                    } else {
System.err.println("IG: " + "METHOD: " + getSignatureString(n));
                    }

                    super.visit(n, arg);
                }

                @Override
                public void visit(ConstructorDeclaration n, Void arg) {

                    if (n.getParentNode().get() instanceof ClassOrInterfaceDeclaration) {
                        type.getType(((ClassOrInterfaceDeclaration) n.getParentNode().get()).getNameAsString()).flatMap(t -> t.getMethod(getSignatureString(n))).ifPresent(m -> {
                            m.getCommentAsString().ifPresent(s -> {

                                n.setComment(new JavadocComment(m.getInnerCommentAsString().get()));
                                System.err.println("RC: " + "CONSTRUCTOR: " + getSignatureString(n));
                            });

                            // fix parameter names (only names at declaration)
                            Streams.zip(m.getParameterList().stream(), n.getParameters().stream(),
                                    Pair::new
                            ).filter(p -> !p.a.getName().equals(p.b.getNameAsString())).forEach(p -> {
                                System.err.println("RN: " + "PARAM: " + p.b.getNameAsString() + " -> " + p.a.getName());
//                                    p.b.setName(p.a.getName()); // TODO
                            });
                        });
                    } else {
System.err.println("IG: " + "CONSTRUCTOR: " + getSignatureString(n));
                    }

                    super.visit(n, arg);
                }

                /** */
                String getSignatureString(MethodDeclaration n) {
                    StringBuilder sb = new StringBuilder(n.getNameAsString());
                    sb.append("(");
                    n.getParameters().forEach(p -> sb.append(Type.getSignatureString(tf.getFullyQualifiedName(p.getType().toString()))));
                    sb.append(")");
//System.err.println("RT: " + n.getType());
                    sb.append(Type.getSignatureString(tf.getFullyQualifiedName(n.getType().toString())));
//System.err.println("SG: " + sb.toString());
                    return sb.toString();
                }

                /** */
                String getSignatureString(ConstructorDeclaration n) {
                    StringBuilder sb = new StringBuilder(n.getNameAsString());
                    sb.append("(");
                    n.getParameters().forEach(p -> sb.append(Type.getSignatureString(tf.getFullyQualifiedName(p.getType().toString()))));
                    sb.append(")");
//System.err.println("SG: " + sb.toString());
                    return sb.toString();
                }

            }, null);

            Path result = Paths.get(outputDir, type.getSourceFilename());
            if (!Files.exists(result.getParent())) {
                Files.createDirectories(result.getParent());
            }
System.err.println("WT: "+ result);
            Files.write(result, unit.toString().getBytes());
        }
    }
}

/* */
