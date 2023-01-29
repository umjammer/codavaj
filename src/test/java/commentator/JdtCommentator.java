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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.antlr.v4.runtime.misc.Pair;
import org.codavaj.process.docparser.DocParser;
import org.codavaj.type.Type;
import org.codavaj.type.TypeFactory;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TextElement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;

import com.google.common.collect.Streams;
import vavi.util.Debug;


/**
 * commentator using eclipse jdt (replace comment, refactoring parameter name: failed)
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2019/05/10 umjammer initial version <br>
 */
public class JdtCommentator {

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
Debug.println("JdtCommentator: " + args[0]);
        JdtCommentator app = new JdtCommentator();
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

            Path sourcePath = Paths.get(sourceDir, type.getSourceFilename());
            if (!Files.exists(sourcePath)) {
//                WriterUtils.print(type, Files.newBufferedWriter(source));
System.err.println("SK: " + sourcePath);
                continue;
            }

            ASTParser parser = ASTParser.newParser(AST.JLS17);
            String sourceString = new String(Files.readAllBytes(sourcePath));
            parser.setSource(sourceString.toCharArray());
            CompilationUnit unit = (CompilationUnit) parser.createAST(new NullProgressMonitor());
            unit.recordModifications();

            AST ast = unit.getAST();

            unit.accept(new ASTVisitor(true) {
                @Override
                public boolean visit(TypeDeclaration n) {
//                    System.out.println("----");
//                    System.out.println("CLASS: " + n.getNameAsString());
//                    n.getComment().ifPresent(v -> {
//                        System.out.println("OLD:");
//                        System.out.println(v);
//                    });

                    type.getType(n.getName().getIdentifier()).ifPresent(t -> t.getCommentAsString().ifPresent(s -> {
//                            System.out.println("--");
//                            System.out.println("NEW:");
//                            System.out.println(s);

                        n.setJavadoc(getJavadoc(t.getInnerCommentAsString().get()));
System.err.println("RC: " + "CLASS: " + n.getName());
                    }));

                    return true;
                }

                @Override
                public boolean visit(FieldDeclaration n) {
                    for (Object o : n.fragments()) {
                        VariableDeclarationFragment v = (VariableDeclarationFragment) o;

                        if (n.getParent() instanceof TypeDeclaration) {
                            type.getType(((TypeDeclaration) n.getParent()).getName().getIdentifier()).flatMap(t -> t.getField(v.getName().toString())).ifPresent(f -> f.getCommentAsString().ifPresent(s -> {
//                                    System.out.println("--");
//                                    System.out.println("NEW:");
//                                    System.out.println(s);

                                n.setJavadoc(getJavadoc(f.getInnerCommentAsString().get()));
                                System.err.println("RC: " + "FIELD: " + v.getName());
                            }));
                        } else {
System.err.println("IG: " + "FIELD: " + v.getName());
                        }
                    }

                    return true;
                }

                // TODO native not comes
                @SuppressWarnings("unchecked")
                @Override
                public boolean visit(MethodDeclaration n) {
//                    System.out.println("----");
//                    System.out.println("METHOD: " + n.getDeclarationAsString());
//                    n.getComment().ifPresent(v -> {
//                        System.out.println("OLD:");
//                        System.out.println(v);
//                    });

                    if (n.getParent() instanceof TypeDeclaration) {
                        type.getType(((TypeDeclaration) n.getParent()).getName().getIdentifier()).flatMap(t -> t.getMethod(getSignatureString(n))).ifPresent(m -> {
                            m.getCommentAsString().ifPresent(s -> {
//                                System.out.println("--");
//                                System.out.println("NEW:");
//                                System.out.println(s);

                                n.setJavadoc(getJavadoc(m.getInnerCommentAsString().get()));
                                System.err.println("RC: " + "METHOD: " + getSignatureString(n));
                            });

                            Streams.zip(m.getParameterList().stream(), ((List<SingleVariableDeclaration>) n.parameters()).stream(),
                                    Pair::new
                            ).filter(p -> !p.a.getName().equals(p.b.getName().toString())).forEach(p -> {
                                System.err.println("RN: " + "PARAM: " + p.b.getName() + " -> " + p.a.getName());
                                p.b.setName(ast.newSimpleName(p.a.getName())); // TODO this is not refactoring
                            });
                        });
                    } else {
System.err.println("IG: " + "METHOD: " + n.getName());
                    }

                    return true;
                }

                /** TODO \n is not handled well */
                @SuppressWarnings("unchecked")
                Javadoc getJavadoc(String t) {
                    Javadoc c = ast.newJavadoc();
                    List<TagElement> tags = c.tags();
                    TagElement tag = ast.newTagElement();

                    Arrays.asList(t.split("\n")).forEach(l -> {
                        TextElement text = ast.newTextElement();
                        text.setText(l);
                        tag.fragments().add(text);
                    });

                    tags.add(tag);

                    return c;
                }

                /** */
                @SuppressWarnings("unchecked")
                String getSignatureString(MethodDeclaration n) {
                    StringBuilder sb = new StringBuilder(n.getName().getIdentifier());
                    sb.append("(");
                    n.parameters().forEach(p -> sb.append(Type.getSignatureString(tf.getFullyQualifiedName(SingleVariableDeclaration.class.cast(p).getType().toString()))));
                    sb.append(")");
                    if (n.getReturnType2() != null) { // constructor
                        sb.append(Type.getSignatureString(tf.getFullyQualifiedName(n.getReturnType2().toString())));
                    }
//System.err.println("SG: "+ sb.toString());
                    return sb.toString();
                }

            });

            Document document = new Document(sourceString);
            TextEdit edit = unit.rewrite(document, null);
            edit.apply(document);

            Path result = Paths.get(outputDir, type.getSourceFilename());
            if (!Files.exists(result.getParent())) {
                Files.createDirectories(result.getParent());
            }
System.err.println("WR: "+ result);

            Files.write(result, document.get().getBytes());
        }
    }
}

/* */
