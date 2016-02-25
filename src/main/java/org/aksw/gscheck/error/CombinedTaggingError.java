package org.aksw.gscheck.error;

import java.util.Collections;
import java.util.List;

import org.aksw.gerbil.dataset.DatasetConfiguration;
import org.aksw.gerbil.dataset.impl.nif.NIFFileDatasetConfig;
import org.aksw.gerbil.datatypes.ExperimentType;
import org.aksw.gerbil.exceptions.GerbilException;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.data.NamedEntity;
import org.aksw.gerbil.transfer.nif.data.StartPosBasedComparator;
import org.aksw.gscheck.corrections.NamedEntityCorrections;
import org.aksw.gscheck.corrections.NamedEntityCorrections.Check;
import org.aksw.gscheck.errorutils.DocumentProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;

import edu.stanford.nlp.ling.CoreLabel;

public class CombinedTaggingError implements ErrorChecker {
	private static final Logger LOGGER = LoggerFactory.getLogger(CombinedTaggingError.class);

	/*
	 * private static final DatasetConfiguration DATASET = new
	 * NIFFileDatasetConfig("DBpedia",
	 * "gerbil_data/datasets/spotlight/dbpedia-spotlight-nif.ttl", false,
	 * ExperimentType.A2KB);
	 */
	static String substring;
	static NamedEntityCorrections a, b;
	static DocumentProcessor dp = new DocumentProcessor();

	public void CombinedTagger(List<Document> documents) throws GerbilException {
		// List<Document> documents =
		// DATASET.getDataset(ExperimentType.A2KB).getInstances();
		LOGGER.info(" COMBINED TAGGER MODULE RUNNING");

		for (Document doc : documents) {
			String text = doc.getText();

			List<CoreLabel> eligible_makrings = dp.Noun_Ad_Extracter(text);
			List<NamedEntityCorrections> entities = doc.getMarkings(NamedEntityCorrections.class);
			Collections.sort(entities, new StartPosBasedComparator());
			if (entities.size() > 0) {
				b = entities.get(0);
				for (int i = 1; i < entities.size(); ++i) {
					a = b;
					b = entities.get(i); // make sure that the entities are not
											// overlapping
					if ((a.getStartPosition() + a.getLength()) <= b.getStartPosition()) {
						substring = text.substring(a.getStartPosition() + a.getLength(), b.getStartPosition());
						if (substring.matches("[\\s]*")) {
							String[] arr = text.substring(a.getStartPosition(), b.getStartPosition() + b.getLength())
									.split(" ");
							for (String x : arr) {
								for (CoreLabel z : eligible_makrings) {
									if (z.get(TextAnnotation.class).equals(x)) {
										System.out.println(
												z.get(TextAnnotation.class) + " " + z.get(PartOfSpeechAnnotation.class)
														+ " " + z.beginPosition() + " " + z.endPosition());
									}
								}
							}

							/*
							 * System.out.println(
							 * "I would connect two entities to a single large entity \""
							 * + text.substring(a.getStartPosition(),
							 * b.getStartPosition() + b.getLength()) + "\".");
							 */
							entities.get(i).setResult(Check.NEED_TO_PAIR);
							entities.get(i).setPartner(a);
						}

					}
				}
			}
			eligible_makrings.clear();
		}

	}

	@Override
	public void check(List<Document> documents) throws GerbilException {
		// TODO Auto-generated method stub
		this.CombinedTagger(documents);
	}
}
