/**
 * This file is part of General Entity Annotator Benchmark.
 *
 * General Entity Annotator Benchmark is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * General Entity Annotator Benchmark is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with General Entity Annotator Benchmark.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.aksw.simba.eaglet.vocab;

import org.aksw.simba.eaglet.entitytypemodify.NamedEntityCorrections.Check;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public class EAGLET {

    private static final Logger LOGGER = LoggerFactory.getLogger(EAGLET.class);

    protected static final String uri = "http://gerbil.aksw.org/eaglet/vocab#";

    /**
     * returns the URI for this schema
     * 
     * @return the URI for this schema
     */
    public static String getURI() {
        return uri;
    }

    protected static final Resource resource(String local) {
        return ResourceFactory.createResource(uri + local);
    }

    protected static final Property property(String local) {
        return ResourceFactory.createProperty(uri, local);
    }

    public static final Resource Inserted = resource("Inserted");
    public static final Resource Deleted = resource("Deleted");
    public static final Resource Good = resource("Good");
    public static final Resource NeedToPair = resource("NeedToPair");
    public static final Resource Overlaps = resource("Overlaps");
    public static final Resource Completed = resource("Completed");
    public static final Resource InvalidUri = resource("InvalidUri");
    public static final Resource OutdatedUri = resource("OutdatedUri");
    public static final Resource DisambiguationUri = resource("DisambiguationUri");

    public static final Property hasCheckResult = property("hasCheckResult");
    public static final Property hasPairPartner = property("hasPairPartner");

    public static Resource getCheckResult(Check checkResult) {
        switch (checkResult) {
        case INSERTED:
            return Inserted;
        case DELETED:
            return Deleted;
        case GOOD:
            return Good;
        case NEED_TO_PAIR:
            return NeedToPair;
        case OVERLAPS:
            return Overlaps;
        case COMPLETED:
            return Completed;
        case INVALID_URI:
            return InvalidUri;
        case OUTDATED_URI:
            return OutdatedUri;
        case DISAMBIG_URI:
            return DisambiguationUri;
        }
        LOGGER.error("Got an unknown matching type: " + checkResult.name());
        return null;
    }
}
