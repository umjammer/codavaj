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

import org.antlr.v4.runtime.misc.Pair;
import org.codavaj.process.docparser.DocParser;
import org.codavaj.type.Type;
import org.codavaj.type.TypeFactory;

import com.google.common.collect.Streams;
import com.netflix.rewrite.ast.Tr.CompilationUnit;
import com.netflix.rewrite.ast.Tr.Empty;
import com.netflix.rewrite.ast.Tr.MethodDecl;
import com.netflix.rewrite.ast.Tr.VariableDecls;
import com.netflix.rewrite.ast.visitor.AstVisitor;
import com.netflix.rewrite.parse.OracleJdkParser;
import com.netflix.rewrite.parse.Parser;


/**
 * Test03. using netflix rewrite (replace comment, refactoring parameter name: wip)
 *
 * <li> netflix rewrite cannot handle comments???
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2019/05/10 umjammer initial version <br>
 */
public class Test03 {

    /** */
    public TypeFactory analyze(String javadocdir, List<String> externalLinks) throws Exception {
        DocParser dp = new DocParser();
        dp.setJavadocClassName("quicktime\\.[A-Z]\\w+$");
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

        Test03 app = new Test03();
        TypeFactory tf = app.analyze(javadocDir, el);

        Parser parser = new OracleJdkParser();

        for (Type type : tf.getTypes()) {

            Path sourcePath = Paths.get(sourceDir, type.getSourceFilename());
            if (!Files.exists(sourcePath)) {
System.err.println("SK: " + sourcePath);
                continue;
            }

            CompilationUnit unit = parser.parse(new String(Files.readAllBytes(sourcePath)));

            new AstVisitor<Void>(Void.class.cast(null)) {

                @Override
                public Void visitMethod(MethodDecl n) {

                        type.getMethod(getSignatureString(n)).ifPresent(m -> {

                            Streams.zip(m.getParameterList().stream(), n.getParams().getParams().stream(),
                                (a, b) -> new Pair<>(a, b)
                            ).filter(p -> {
                                if (Empty.class.isInstance(p.b)) {
                                    return false;
                                } else if (VariableDecls.class.isInstance(p.b)) {
                                    // parameter must have one variable
                                    String name = VariableDecls.class.cast(p.b).getVars().get(0).getSimpleName().toString();
                                    return !p.a.getName().equals(name);
                                } else {
System.err.println("?1: " + p.b);
                                    return false;
                                }
                            }).forEach(p -> {
                                String name = VariableDecls.class.cast(p.b).getVars().get(0).getSimpleName().toString();
System.err.println("RN: " + "PARAM: " + name + " -> " + p.a.getName() + " \t\t/ " + getSignatureString(n));
                                // TODO this only rename a parameter name...
                                String diff = unit.refactor().changeFieldName(VariableDecls.class.cast(p.b), p.a.getName()).diff();
System.out.println(diff);
                            });
                        });

                    return null;
                }

                /** */
                String getSignatureString(MethodDecl n) {
                    StringBuilder sb = new StringBuilder(n.getName().getSimpleName().toString());
                    sb.append("(");
                    n.getParams().getParams().forEach(p -> {
//System.err.println("MP: "+ p);
                        if (Empty.class.isInstance(p)) {
                        } else if (VariableDecls.class.isInstance(p)) {
                            com.netflix.rewrite.ast.Type t = VariableDecls.class.cast(p).getTypeExpr().getType();
                            sb.append(getSignatureString(t));
                        }
                    });
                    sb.append(")");
                    if (n.getReturnTypeExpr() != null) { // constructor
//System.err.println("MR: "+ n.getReturnTypeExpr());
                        com.netflix.rewrite.ast.Type t = n.getReturnTypeExpr().getType();
                        sb.append(getSignatureString(t));
                    }
//System.err.println("SG: "+ sb.toString());
                    return sb.toString();
                }

                /** */
                String getSignatureString(com.netflix.rewrite.ast.Type t) {
                    String name = null;
                    if (com.netflix.rewrite.ast.Type.Primitive.class.isInstance(t)) {
                        name = com.netflix.rewrite.ast.Type.Primitive.class.cast(t).getKeyword();
                    } else if (com.netflix.rewrite.ast.Type.Class.class.isInstance(t)) {
                        com.netflix.rewrite.ast.Type.Class c = com.netflix.rewrite.ast.Type.Class.class.cast(t);
                        name = c.getFullyQualifiedName();
                    } else {
System.err.println("?2: " + t);
                    }
                    return Type.getSignatureString(name);
                }
            }.visit(unit);

            Path result = Paths.get(outputDir, type.getSourceFilename());
            if (!Files.exists(result.getParent())) {
                Files.createDirectories(result.getParent());
            }
System.err.println("WR: "+ result);
            Files.write(result, unit.print().getBytes());
        }
    }
}

/* */
