package org.aksw.simba.eaglet.completion;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.aksw.gerbil.annotator.A2KBAnnotator;
import org.aksw.gerbil.exceptions.GerbilException;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.MeaningSpan;
import org.aksw.gerbil.transfer.nif.data.StartPosBasedComparator;
import org.aksw.simba.eaglet.documentprocessor.StanfordParsedMarking;
import org.aksw.simba.eaglet.entitytypemodify.EntityTypeChange;
import org.aksw.simba.eaglet.entitytypemodify.NamedEntityCorrections;
import org.aksw.simba.eaglet.entitytypemodify.NamedEntityCorrections.Check;
import org.aksw.simba.eaglet.errorutils.Token_StartposbasedComparator;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;

public class MissingEntityCompletion implements GoldStandardCompletion {

	private List<A2KBAnnotator> annotators;
	private static final Logger LOGGER = LoggerFactory.getLogger(MissingEntityCompletion.class);

	public MissingEntityCompletion(List<A2KBAnnotator> annotators) {
		this.annotators = annotators;
	}

	public void addMissingEntity(List<List<MeaningSpan>> annotator_result, Document doc) throws GerbilException {

		Map<String, List<NamedEntityCorrections>> map = generate_map(doc);
		List<List<NamedEntityCorrections>> annotatorresult = adjustAnnotatorresult(doc, annotator_result);

		// iterate over all annotatorResult
		for (List<NamedEntityCorrections> list : annotatorresult) {
			for (NamedEntityCorrections annotatorentity : list) {

				// if this token matches a first token of an entity
				if (map.containsKey(annotatorentity.getEntity_name())) {
					// get the list of possible entities
					List<NamedEntityCorrections> current_list = map.get(annotatorentity.getEntity_name());
					// iterate over the list of possible entities
					for (NamedEntityCorrections docentity : current_list) {
						if (docentity.getStartPosition() == annotatorentity.getStartPosition()) {
							if (docentity.getNumber_of_lemma() < annotatorentity.getNumber_of_lemma()) {
								annotatorentity.setResult(Check.COMPLETED);
								doc.addMarking(annotatorentity);
							}

						} else {
							annotatorentity.setResult(Check.COMPLETED);
							doc.addMarking(annotatorentity);
						}
					}
				}
			}
		}
	}

	public List<List<NamedEntityCorrections>> adjustAnnotatorresult(Document doc,
			List<List<MeaningSpan>> annotator_resutl) {
		List<List<NamedEntityCorrections>> formatedList = new ArrayList<List<NamedEntityCorrections>>();
		List<List<NamedEntityCorrections>> formatedList2 = new ArrayList<List<NamedEntityCorrections>>();
		EntityTypeChange ec = new EntityTypeChange();
		for (List<MeaningSpan> lis : annotator_resutl) {
			List<NamedEntityCorrections> new_list = ec.changeListType(lis);
			formatedList.add(new_list);
		}
		for (List<NamedEntityCorrections> list : formatedList) {
			formatedList2.add(nameEntity(doc, list));
		}

		return formatedList2;
	}

	public Map<String, List<NamedEntityCorrections>> generate_map(Document doc) {
		Map<String, List<NamedEntityCorrections>> map = new HashMap<String, List<NamedEntityCorrections>>();
		List<NamedEntityCorrections> entities = doc.getMarkings(NamedEntityCorrections.class);

		entities = nameEntity(doc, entities);

		for (NamedEntityCorrections entity : entities) {
			entity.setDoc(doc.getDocumentURI());
			if (map.containsKey(entity.getEntity_name())) {
				map.get(entity.getEntity_name()).add(entity);

			}

			else {
				List<NamedEntityCorrections> sub_entity = new ArrayList<NamedEntityCorrections>();
				sub_entity.add(entity);
				map.put(entity.getEntity_name(), sub_entity);
			}
		}

		return map;
	}

	public List<NamedEntityCorrections> nameEntity(Document doc, List<NamedEntityCorrections> entities) {

		List<StanfordParsedMarking> stanfordAnns = doc.getMarkings(StanfordParsedMarking.class);

		StanfordParsedMarking stanfordAnn = stanfordAnns.get(0);

		List<CoreLabel> tokens = stanfordAnn.getAnnotation().get(TokensAnnotation.class);
		if (stanfordAnns.size() != 1) {
			// TODO PANIC!!!!
			LOGGER.error(" Parser not working ");
		}
		String text = doc.getText();
		// 1. create Bitset containing the ne positions and gather ne positions
		BitSet nePositions = new BitSet(text.length());
		int start[] = new int[entities.size()];
		int end[] = new int[entities.size()];

		Collections.sort(entities, new StartPosBasedComparator());
		for (int i = 0; i < entities.size(); ++i) {
			start[i] = entities.get(i).getStartPosition();
			end[i] = entities.get(i).getStartPosition() + entities.get(i).getLength();
			nePositions.set(start[i], end[i]);
		}
		// 2. Iterate over the tokens and search for tokens that are inside of
		// ne boarders
		@SuppressWarnings("unchecked")
		List<CoreLabel> entityTokens[] = new List[entities.size()];
		for (CoreLabel token : tokens) {
			if (nePositions.get(token.beginPosition()) || nePositions.get(token.endPosition() - 1)) {
				// search for matching named entities
				int pos = 0;
				while ((pos < start.length) && (start[pos] <= token.endPosition())) {
					// if the token and the
					if ((start[pos] <= token.beginPosition()) && (end[pos] >= token.endPosition())) {
						// nes.get(pos) is the matching ne
						// add the token to the list of tokens of ne
						if (entityTokens[pos] == null) {
							entityTokens[pos] = new ArrayList<CoreLabel>();
						}
						entityTokens[pos].add(token);
					}
					++pos;
				}
			}
		}
		Token_StartposbasedComparator comparator = new Token_StartposbasedComparator();

		// setting text for entity
		NamedEntityCorrections currentNamedEntity;
		for (int i = 0; i < entities.size(); i++) {
			if (entityTokens[i] == null) {
				// TODO print error because the entities has got no tokens :(
			} else {
				// sorting the token list
				Collections.sort(entityTokens[i], comparator);
				currentNamedEntity = entities.get(i);
				currentNamedEntity.setEntity_name(entityTokens[i].get(0).get(LemmaAnnotation.class));
				currentNamedEntity.entity_text = new String[entityTokens[i].size()];
				for (int j = 0; j < entityTokens[i].size(); ++j) {
					currentNamedEntity.entity_text[j] = entityTokens[i].get(j).get(LemmaAnnotation.class);
				}
				currentNamedEntity.setNumber_of_lemma(entityTokens[i].size());
			}
		}

		return entities;
	}

	@Override
	public void check(List<Document> documents) throws GerbilException {

		List<List<MeaningSpan>> annotatorResults = new ArrayList<List<MeaningSpan>>();
		for (Document document : documents) {
			annotatorResults.clear();
			for (A2KBAnnotator annotator : annotators) {
				annotatorResults.add(annotator.performA2KBTask(document));
			}
			addMissingEntity(annotatorResults, document);
		}
	}
}
