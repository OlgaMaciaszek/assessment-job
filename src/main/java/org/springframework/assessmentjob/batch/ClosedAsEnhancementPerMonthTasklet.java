package org.springframework.assessmentjob.batch;

import java.util.List;
import java.util.Map;

import org.springframework.assessmentjob.configuration.ProjectAssessmentProperties;
import org.springframework.assessmentjob.util.ReportKey;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestOperations;

/**
 * @author Olga Maciaszek-Sharma
 */
@Component
public class ClosedAsEnhancementPerMonthTasklet extends BaseGithubSearchTasklet{

	public ClosedAsEnhancementPerMonthTasklet(RestOperations restTemplate, Map<ReportKey, List<Long>> report, ProjectAssessmentProperties properties) {
		super(restTemplate, report, properties);
	}

	@Override
	public String getQuery() {
		return properties.getProjectRepo() + " is:issue closed:%s is:closed label:\"enhancement\"";
	}

	@Override
	public String getResultKey() {
		return "total_count";
	}

	@Override
	public ReportKey getReportKey() {
		return ReportKey.CLOSED_AS_ENHANCEMENT;
	}
}
