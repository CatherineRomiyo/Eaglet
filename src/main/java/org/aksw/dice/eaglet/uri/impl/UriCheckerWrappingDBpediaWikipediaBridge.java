package org.aksw.dice.eaglet.uri.impl;

import java.util.Set;

import org.aksw.dice.eaglet.entitytypemodify.NamedEntityCorrections.Check;
import org.aksw.dice.eaglet.uri.UriChecker;
import org.aksw.gerbil.semantic.sameas.impl.wiki.WikiDbPediaBridgingSameAsRetriever;

public class UriCheckerWrappingDBpediaWikipediaBridge implements UriChecker {

    protected UriChecker decorated;
    protected WikiDbPediaBridgingSameAsRetriever bridge = new WikiDbPediaBridgingSameAsRetriever();

    public UriCheckerWrappingDBpediaWikipediaBridge(UriChecker decorated) {
        this.decorated = decorated;
    }

    @Override
    public Check checkUri(String uri) {
        Set<String> retrievedUris = bridge.retrieveSameURIs(uri);
        Check result;
        for (String retrievedUri : retrievedUris) {
            result = decorated.checkUri(retrievedUri);
            if (result != Check.GOOD) {
                return result;
            }
        }
        return Check.GOOD;
    }

}