package org.springframework.assessmentjob.batch;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestOperations;

/**
 * @author Olga Maciaszek-Sharma
 */
@Component
public class ClosedAsQuestionPerMonthTasklet extends BaseGithubSearchTasklet {

	public ClosedAsQuestionPerMonthTasklet(RestOperations restTemplate,
			@Value("${spring.project.repo}") String repo,
			Map<String, List<Integer>> report) {
		super(restTemplate, repo, report);
	}

	@Override
	public String getQuery() {
		return repo + " is:issue closed:%s is:closed label:\"question\"";
	}

	@Override
	public String getResultKey() {
		return "total_count";
	}

	@Override
	public String getReportKey() {
		return "question";
	}
}
