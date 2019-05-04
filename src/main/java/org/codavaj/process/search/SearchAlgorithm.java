package org.codavaj.process.search;

import java.util.List;

import org.codavaj.AbstractLogger;
import org.codavaj.type.TypeFactory;
import org.codavaj.type.Package;

public class SearchAlgorithm extends AbstractLogger {

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

        info( "Attempting " + javadocRoot.getName() + " -> " + jarRoot.getName());
        ctx.setMap(javadocRoot, jarRoot);

        if ( matchAll( javadocRoot, jarRoot ) ) {
            info( "Matched All " + javadocRoot.getName() +" -> "  + jarRoot.getName());
        } else {
            info( "NOT Matched All " + javadocRoot.getName() +" -> "  + jarRoot.getName());
            ctx.removeMap(javadocRoot, jarRoot);
        }

    }

    private boolean matchAll( Package javadocPackage, Package jarPackage ) {
        // a package matches another if all it's subpackages can be made to
        // match the other too.
        List<?> jarsubPackages = jarPackage.getPackages();
        List<?> docsubPackages = javadocPackage.getPackages();

        boolean matchesAllSubPackages = true;
        for( int i = 0; i < docsubPackages.size(); i++ ) {
            Package docsubPackage = (Package)docsubPackages.get(i);

            if ( !matchAny( docsubPackage, jarsubPackages)) {
                info( "Package " + docsubPackage.getName() + " doesn't match any sub packages of " + jarPackage.getName());
                matchesAllSubPackages = false;
                break;
            }

        }
        return matchesAllSubPackages;
    }

    private boolean matchAny( Package javadocPackage, List<?> jarPackages ) {
        for( int i=0; i < jarPackages.size(); i++ ) {
            Package jarPackage = (Package)jarPackages.get(i);

            if ( ctx.getMap(jarPackage) != null ) {
                info("Already mapped " + jarPackage.getName());
                continue;
            }

            info( "Attempting " + javadocPackage.getName() + " -> " + jarPackage.getName());
            ctx.setMap(javadocPackage, jarPackage );
            if ( matchAll( javadocPackage, jarPackage ) ) {
                info( "Matched ALL " + javadocPackage.getName() + " -> " + jarPackage.getName());
                return true;
            }
            ctx.removeMap(javadocPackage, jarPackage);
        }
        return false;
    }
}
