package uk.ac.man.entitytagger.evaluate;

import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.man.entitytagger.Mention;
import uk.ac.man.entitytagger.evaluate.Evaluate.Tag;
import martin.common.compthreads.Problem;

/**
 * Simple class for parallelising evaluations
 * @author Martin
 *
 */
public class EvaluateProblem implements Problem<Result[]> {

	private Map<String, List<Mention>> mainTags;
	private Map<String, Map<String, List<Tag>>> mainTagsByDoc;
	private Map<String, List<Mention>> refTags;
	private Map<String, Map<String, List<Tag>>> refTagsByDoc;
	private Map<String, String> articleIDs;
	private Set<String> ids;
	private String[] docArray;
	private int numDocsToRun;
	private String title;

	public EvaluateProblem(
			Map<String, List<Mention>> mainTags, 
			Map<String, Map<String, List<Tag>>> mainTagsByDoc, 
			Map<String, List<Mention>> refTags, 
			Map<String, Map<String, List<Tag>>> refTagsByDoc, 
			Map<String,String> articleIDs, 
			Set<String> ids,
			int numDocsToRun,
			String[] docArray,
			String title
			){
		this.mainTags = mainTags;
		this.mainTagsByDoc = mainTagsByDoc;
		this.refTags = refTags;
		this.refTagsByDoc = refTagsByDoc;
		this.articleIDs = articleIDs;
		this.ids= ids;
		this.docArray = docArray;
		this.numDocsToRun = numDocsToRun;
		this.title = title;
	}
	
	public Result[] compute() {
		Set<String> documents = Evaluate.getDocumentSelection(docArray, numDocsToRun);
		Result[] r = new Evaluate().process(mainTags, mainTagsByDoc, refTags, refTagsByDoc, articleIDs, ids, null, false, documents,title);
		return r;
	}
}
