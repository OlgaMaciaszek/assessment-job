package org.springframework.assessmentjob.batch;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import org.springframework.assessmentjob.util.DateCalculationUtils;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestOperations;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class HasMilestonePerMonth implements Tasklet {

	private final RestOperations restTemplate;

	private final Map<String, List<Integer>> report;

	private final String repo;

	private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

	public HasMilestonePerMonth(RestOperations restTemplate,
			@Value("${spring.project.repo}") String repo,
			Map<String, List<Integer>> report) {
		this.restTemplate = restTemplate;
		this.repo = repo;
		this.report = report;
	}

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

		String query = repo + " is:issue created:%s";
		int milestoneCount = 0;

		// For each month
		Date startDate = DateCalculationUtils.getFirstMonthStartDate();
		Date endDate = DateCalculationUtils.getFirstMonthEndDate();

		List<Integer> milestoneValues = new ArrayList<>(13);

		for (int i = 0; i < 13; i++) {
			UriComponentsBuilder builder = UriComponentsBuilder
					.fromHttpUrl("https://api.github.com/search/issues")
					.queryParam("q", String
							.format(query, getDateString(startDate, endDate)));

			URI uri = builder.build().encode().toUri();
			System.out.println(">> " + uri.toString());
			HttpEntity<String> response = this.restTemplate
					.getForEntity(uri, String.class);

			Map<String, Object> result = new ObjectMapper()
					.readValue(response.getBody(), HashMap.class);
			List<Map<String, Object>> issues = (List<Map<String, Object>>) result
					.get("items");

			for (Map<String, Object> issue : issues) {
				if (issue.get("milestone") != null

				) {
					if(
							!StringUtils
									.equalsIgnoreCase(((Map<String, Object>) issue.get("milestone"))
											.get("title").toString(), "backlog")){
						milestoneCount++;
					}

				}

			}

			milestoneValues.add(milestoneCount);

			startDate = DateUtils.addMonths(startDate, -1);
			endDate = DateUtils.addMonths(endDate, -1);
			Calendar instance = Calendar.getInstance();
			instance.setTime(endDate);
			instance.set(Calendar.DAY_OF_MONTH, instance
					.getActualMaximum(Calendar.DAY_OF_MONTH));
			endDate = instance.getTime();

			milestoneCount = 0;
		}

		report.put("milestones", milestoneValues);

		return RepeatStatus.FINISHED;
	}

	private String getDateString(Date startDate, Date endDate) {

		return String.format("%s..%s", formatter.format(startDate), formatter.format(endDate));
	}
}