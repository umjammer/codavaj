package org.codavaj.process.search;

import java.util.List;
import java.util.logging.Logger;

import org.codavaj.type.Package;
import org.codavaj.type.TypeFactory;

public class SearchAlgorithm {

    private static final Logger logger = Logger.getLogger(SearchAlgorithm.class.getName());

    private TypeFactory docFactory;
    private TypeFactory jarFactory;
    private SearchContext ctx;

    public SearchAlgorithm( TypeFactory javadoc, TypeFactory jar, SearchContext ctx ) {
        this.docFactory = javadoc;
        this.jarFactory = jar;
        this.ctx = ctx;
    }

    public void search() {
        Package javadocRoot = docFactory.getDefaultPackage();
        Package jarRoot = jarFactory.getDefaultPackage();

        logger.info("Attempting " + javadocRoot.getName() + " -> " + jarRoot.getName());
        ctx.setMap(javadocRoot, jarRoot);

        if ( matchAll(javadocRoot, jarRoot )) {
            logger.info( "Matched All " + javadocRoot.getName() +" -> "  + jarRoot.getName());
        } else {
            logger.info("NOT Matched All " + javadocRoot.getName() +" -> "  + jarRoot.getName());
            ctx.removeMap(javadocRoot, jarRoot);
        }

    }

    private boolean matchAll( Package javadocPackage, Package jarPackage ) {
        // a package matches another if all it's subpackages can be made to
        // match the other too.
        List<Package> jarsubPackages = jarPackage.getPackages();
        List<Package> docsubPackages = javadocPackage.getPackages();

        boolean matchesAllSubPackages = true;
        for (Package docsubPackage : docsubPackages) {
            if (!matchAny(docsubPackage, jarsubPackages)) {
                logger.info("Package " + docsubPackage.getName() + " doesn't match any sub packages of " + jarPackage.getName());
                matchesAllSubPackages = false;
                break;
            }

        }
        return matchesAllSubPackages;
    }

    private boolean matchAny( Package javadocPackage, List<Package> jarPackages ) {
        for (Package jarPackage : jarPackages) {
            if (ctx.getMap(jarPackage) != null) {
                logger.info("Already mapped " + jarPackage.getName());
                continue;
            }

            logger.info("Attempting " + javadocPackage.getName() + " -> " + jarPackage.getName());
            ctx.setMap(javadocPackage, jarPackage);
            if (matchAll(javadocPackage, jarPackage)) {
                logger.info("Matched ALL " + javadocPackage.getName() + " -> " + jarPackage.getName());
                return true;
            }
            ctx.removeMap(javadocPackage, jarPackage);
        }
        return false;
    }
}
