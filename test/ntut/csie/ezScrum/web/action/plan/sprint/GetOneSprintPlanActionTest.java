package ntut.csie.ezScrum.web.action.plan.sprint;

import java.io.File;
import java.io.IOException;

import ntut.csie.ezScrum.issue.sql.service.core.Configuration;
import ntut.csie.ezScrum.issue.sql.service.core.InitialSQL;
import ntut.csie.ezScrum.test.TestTool;
import ntut.csie.ezScrum.test.CreateData.CreateProject;
import ntut.csie.ezScrum.test.CreateData.CreateSprint;
import ntut.csie.ezScrum.web.dataObject.ProjectObject;
import ntut.csie.ezScrum.web.dataObject.SprintObject;
import servletunit.struts.MockStrutsTestCase;

public class GetOneSprintPlanActionTest extends MockStrutsTestCase {
	private CreateProject mCP;
	private CreateSprint mCS;
	private Configuration mConfig;
	private final String mActionPath = "/GetSprintPlan";
	private ProjectObject mProject;

	public GetOneSprintPlanActionTest(String testMethod) {
		super(testMethod);
	}

	protected void setUp() throws Exception {
		mConfig = new Configuration();
		mConfig.setTestMode(true);
		mConfig.save();

		// 初始化 SQL
		InitialSQL ini = new InitialSQL(mConfig);
		ini.exe();

		// 新增一個測試專案
		mCP = new CreateProject(1);
		mCP.exeCreateForDb();
		mProject = mCP.getAllProjects().get(0);

		// 新增兩個Sprint
		mCS = new CreateSprint(2, mCP);
		mCS.exe();

		super.setUp();

		setContextDirectory(new File(mConfig.getBaseDirPath() + "/WebContent")); // 設定讀取的
																					// struts-config
																					// 檔案路徑
		setServletConfigFile("/WEB-INF/struts-config.xml");
		setRequestPathInfo(mActionPath);

		// ============= release ==============
		ini = null;
	}

	protected void tearDown() throws IOException, Exception {
		// 刪除資料庫
		InitialSQL ini = new InitialSQL(mConfig);
		ini.exe();

		mConfig.setTestMode(false);
		mConfig.save();

		// ============= release ==============
		ini = null;
		mCP = null;
		mCS = null;
		mConfig = null;

		super.tearDown();
	}

	public void testShowSprint_1() {
		// ================ set request info ========================
		String projectName = mProject.getName();
		request.setHeader("Referer", "?projectName=" + projectName);
		addRequestParameter("SprintID",
				String.valueOf(mCS.getSprintsId().get(0)));

		// ================ set session info ========================
		request.getSession().setAttribute("UserSession",
				mConfig.getUserSession());

		// ================ 執行 action ======================
		actionPerform();

		// ================ assert ========================
		verifyNoActionErrors();
		verifyNoActionMessages();
		// assert response text
		TestTool testTool = new TestTool();
		StringBuilder expectedResponseText = new StringBuilder();
		expectedResponseText.append("{\"Sprints\":[{");
		expectedResponseText.append("\"Id\":\"" + mCS.getSprintsId().get(0)
				+ "\",");
		expectedResponseText.append("\"Goal\":\"" + mCS.TEST_SPRINT_GOAL + 1
				+ "\",");
		expectedResponseText.append("\"StartDate\":\""
				+ testTool.transformDate(mCS.mToday) + "\",");
		expectedResponseText.append("\"Interval\":\""
				+ CreateSprint.SPRINT_INTERVAL + "\",");
		expectedResponseText.append("\"Members\":\""
				+ CreateSprint.SPRINT_MEMBER + "\",");
		expectedResponseText.append("\"AvaliableDays\":\""
				+ CreateSprint.SPRINT_HOURS_CAN_COMMIT + " hours\",");
		expectedResponseText.append("\"FocusFactor\":\""
				+ CreateSprint.SPRINT_FOCUS_FACTOR + "\",");
		expectedResponseText.append("\"DailyScrum\":\""
				+ mCS.TEST_SPRINT_DAILY_INFO + 1 + "\",");
		expectedResponseText.append("\"DemoDate\":\""
				+ testTool.calcaulateEndDate(CreateSprint.SPRINT_INTERVAL,
						mCS.mToday) + "\",");
		expectedResponseText.append("\"DemoPlace\":\""
				+ CreateSprint.SPRINT_DEMOPLACE + "\",");
		expectedResponseText.append("\"EndDate\":\""
				+ testTool.calcaulateEndDate(CreateSprint.SPRINT_INTERVAL,
						mCS.mToday) + "\"");
		expectedResponseText.append("}]}");
		String actualResponseText = response.getWriterBuffer().toString();
		assertEquals(expectedResponseText.toString(), actualResponseText);
	}

	public void testShowSprint_2() {
		// ================ set request info ========================
		String projectName = mProject.getName();
		request.setHeader("Referer", "?projectName=" + projectName);
		addRequestParameter("lastsprint", String.valueOf(true));

		// ================ set session info ========================
		request.getSession().setAttribute("UserSession",
				mConfig.getUserSession());

		// ================ 執行 action ======================
		actionPerform();

		// ================ assert ========================
		verifyNoActionErrors();
		verifyNoActionMessages();
		SprintObject lastSprint = mCS.getSprints().get(mCS.getSprints().size() - 1);
		// assert response text
		StringBuilder expectedResponseText = new StringBuilder();
		expectedResponseText.append("{\"Sprints\":[{");
		expectedResponseText.append("\"Id\":\"" + "2" + "\",");
		expectedResponseText.append("\"Goal\":\"TEST_SPRINTGOAL_2\",");
		expectedResponseText.append("\"StartDate\":\"" + lastSprint.getStartDateString() + "\",");
		expectedResponseText.append("\"Interval\":\"2\",");
		expectedResponseText.append("\"Members\":\"4\",");
		expectedResponseText.append("\"AvaliableDays\":\"120 hours\",");
		expectedResponseText.append("\"FocusFactor\":\"80\",");
		expectedResponseText.append("\"DailyScrum\":\"TEST_SPRINTDAILYINFO_2\",");
		expectedResponseText.append("\"DemoDate\":\"" + lastSprint.getDemoDateString() + "\",");
		expectedResponseText.append("\"DemoPlace\":\"Lab1321\",");
		expectedResponseText.append("\"EndDate\":\"" + lastSprint.getEndDateString() + "\"");
		expectedResponseText.append("}]}");
		String actualResponseText = response.getWriterBuffer().toString();
		assertEquals(expectedResponseText.toString(), actualResponseText);
	}
}
