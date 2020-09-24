package org.openmrs.module.htmlformentry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.openmrs.Encounter;
import org.openmrs.EncounterProvider;
import org.openmrs.EncounterType;
import org.openmrs.Form;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.api.context.Context;
import org.openmrs.module.ModuleUtil;
import org.openmrs.module.htmlformentry.FormEntryContext.Mode;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartHttpServletRequest;

public abstract class RegressionTestHelper {
	
	private static final String XML_DATASET_PATH = "org/openmrs/module/htmlformentry/include/";
	
	/**
	 * @return will be used to look up the file test/.../include/{formName}.xml
	 */
	public abstract String getFormName();
	
	/**
	 * you probably want to override this
	 * 
	 * @return the labels before all widgets you want to set values for (allows you to refer to "Date:"
	 *         instead of "w1" in setupRequest)
	 */
	public String[] widgetLabels() {
		return new String[0];
	}
	
	/**
	 * Override this if you want to test what the generated html of the blank form looks like.
	 * 
	 * @param html
	 */
	public void testBlankFormHtml(String html) {
	}
	
	/**
	 * (Override this if you want to test the submission of a form.) Set any request parameters that
	 * will be sent in the form submission
	 * 
	 * @param request an empty request for you to populate
	 * @param widgets map from the label you provided in widgetLabels() to the name that form element
	 *            should submit as (which was autogenerated by the html form, e.g. "w3")
	 */
	public void setupRequest(MockHttpServletRequest request, Map<String, String> widgets) {
	}
	
	/**
	 * (Override this if you want to test the submission of a form.)
	 * 
	 * @param results the results of having submitted the request you set up in setupRequest. This will
	 *            contain either validationErrors, or else an encounterCreated
	 */
	public void testResults(SubmissionResults results) {
	}
	
	/**
	 * Optionally override this if you want to generate the form for a different patient, or if you are
	 * testing a patient creation form
	 * 
	 * @return
	 */
	public Patient getPatient() {
		return Context.getPatientService().getPatient(2);
	}
	
	/**
	 * Optionally override this if you want to test out viewing a specific patient rather than the one
	 * that was used earlier in this test case to fill out a form
	 * 
	 * @return
	 * @throws Exception
	 */
	public Patient getPatientToView() throws Exception {
		return null;
	}
	
	/**
	 * Override this and return true if you want to have testViewingPatient run. (If you override
	 * getPatientToView to return something non-null, then you do not need to override this method --
	 * testViewingPatient will be called anyway.)
	 */
	public boolean doViewPatient() {
		return false;
	}
	
	/**
	 * Override this if you want to test out viewing a patient without an encounter
	 * 
	 * @param patient
	 * @param html
	 */
	public void testViewingPatient(Patient patient, String html) {
	}
	
	/**
	 * Optionally override this if you want to test out viewing a specific encounter rather than the one
	 * that was created earlier in this test case.
	 * 
	 * @return
	 * @throws Exception
	 */
	public Encounter getEncounterToView() throws Exception {
		return null;
	}
	
	/**
	 * Override this and return true if you want to have testViewingEncounter run. (If you override
	 * getEncounterToView to return something non-null, then you do not need to override this method --
	 * testViewingEncounter will be called anyway.)
	 */
	public boolean doViewEncounter() {
		return false;
	}
	
	/**
	 * Override this if you want to test out viewing an encounter
	 * 
	 * @param encounter
	 * @param html
	 */
	public void testViewingEncounter(Encounter encounter, String html) {
	}
	
	/**
	 * Override this and return true if you want to have testEditFormHtml, testEditEncounter, etc run.
	 * (If you override {@link #getEncounterToEdit()} to return a non-null value, you don't need to
	 * override this one.)
	 */
	public boolean doEditEncounter() {
		return false;
	}
	
	/**
	 * Override this and return true if you want to have testEditFormHtml, testEditPatient, etc run. (If
	 * you override {@link #getPatientToEdit()} to return a non-null value, you don't need to override
	 * this one.)
	 */
	public boolean doEditPatient() {
		return false;
	}
	
	/**
	 * Override this if you want to edit a different encounter than the one created in the first part of
	 * the test or viewed in the second part. (Returning a non-null value implies doEditEncounter =
	 * true.)
	 * 
	 * @return
	 */
	public Encounter getEncounterToEdit() {
		return null;
	}
	
	/**
	 * Override this if you want to edit a Patient than the one created in the first part of the test or
	 * viewed in the second part. (Returning a non-null value implies doEditPatient = true.)
	 * 
	 * @return
	 */
	public Patient getPatientToEdit() {
		return null;
	}
	
	/**
	 * Override this if you want to test what the generated html of the edit form looks like.
	 * 
	 * @param html
	 */
	public void testEditFormHtml(String html) {
	}
	
	/**
	 * (Override this if you want to test the submission of editing a form.) Set any request parameters
	 * that will be sent in the form submission
	 * 
	 * @param request an empty request for you to populate
	 * @param widgets map from the label you provided in widgetLabelsForEdit() to the name that form
	 *            element should submit as (which was autogenerated by the html form, e.g. "w3")
	 */
	public void setupEditRequest(MockHttpServletRequest request, Map<String, String> widgets) {
	}
	
	/**
	 * @return the labels before all widgets you want to set values for in the edit form
	 * @see #widgetLabels()
	 */
	public String[] widgetLabelsForEdit() {
		return new String[0];
	}
	
	/**
	 * (Override this if you want to test the submission of editing a form.)
	 * 
	 * @param results the results of having submitted the request you set up in setupEditRequest. This
	 *            will contain either validationErrors, or else an encounterCreated
	 */
	public void testEditedResults(SubmissionResults results) {
	}
	
	/**
	 * (Override this if you want to test the an attribute of FormEntrySession in form entry (ENTER)
	 * mode.)
	 * 
	 * @param formEntrySession object, useful in test state of session object
	 */
	public void testFormEntrySessionAttribute(FormEntrySession formEntrySession) {
	}
	
	/**
	 * (Override this if you want to test the an attribute of FormEntrySession in form view mode.)
	 * 
	 * @param formEntrySession object, useful in test state of session object
	 */
	public void testFormViewSessionAttribute(FormEntrySession formEntrySession) {
	}
	
	/**
	 * (Override this if you want to test the an attribute of FormEntrySession in form edit mode.)
	 * 
	 * @param formEntrySession object, useful in test state of session object
	 */
	public void testFormEditSessionAttribute(FormEntrySession formEntrySession) {
	}
	
	public void run() throws Exception {
		// setup the blank form for the specified patient
		Patient patient = getPatient();
		FormEntrySession session = setupFormEntrySession(patient, getFormName());
		testFormEntrySessionAttribute(session);
		String html = session.getHtmlToDisplay();
		testBlankFormHtml(html);
		
		// submit some initial data and test it
		Map<String, String> labeledWidgets = getLabeledWidgets(html, widgetLabels());
		MockHttpServletRequest request = new MockMultipartHttpServletRequest();
		request.setSession(session.getHttpSession());
		setupRequest(request, labeledWidgets);
		Patient patientToView = null;
		Encounter encounterToView = null;
		if (request.getParameterMap().size() > 0) {
			SubmissionResults results = doSubmission(session, request);
			testResults(results);
			patientToView = results.getPatient();
			encounterToView = results.getEncounterCreated();
		}
		
		// view that patient and run tests on it
		Patient overridePatient = getPatientToView();
		boolean doViewPatient = overridePatient != null || doViewPatient();
		if (doViewPatient) {
			if (overridePatient != null)
				patientToView = overridePatient;
			session = setupFormViewSession(patientToView, null, getFormName());
			testFormViewSessionAttribute(session);
			html = session.getHtmlToDisplay();
			testViewingPatient(patientToView, html);
		}
		
		// view that encounter and run tests on that
		Encounter override = getEncounterToView();
		boolean doViewEncounter = override != null || doViewEncounter();
		if (doViewEncounter) {
			if (override != null)
				encounterToView = override;
			
			session = setupFormViewSession(patientToView, encounterToView, getFormName());
			testFormViewSessionAttribute(session);
			html = session.getHtmlToDisplay();
			testViewingEncounter(encounterToView, html);
		}
		
		// edit the encounter, and run tests on that
		override = getEncounterToEdit();
		boolean doEditEncounter = override != null || doEditEncounter();
		
		overridePatient = getPatientToEdit();
		boolean doEditPatient = overridePatient != null || doEditPatient();
		
		if (doEditEncounter || doEditPatient) {
			Encounter toEdit = encounterToView;
			if (override != null)
				toEdit = override;
			
			Patient patientToEdit = patientToView;
			if (overridePatient != null)
				patientToEdit = overridePatient;
			
			session = setupFormEditSession(patientToEdit, toEdit, getFormName());
			testFormEditSessionAttribute(session);
			String editHtml = session.getHtmlToDisplay();
			testEditFormHtml(editHtml);
			
			Map<String, String> labeledWidgetsForEdit = getLabeledWidgets(editHtml, widgetLabelsForEdit());
			MockHttpServletRequest editRequest = createEditRequest(editHtml, session.getHttpSession());
			setupEditRequest(editRequest, labeledWidgetsForEdit);
			if (editRequest.getParameterMap().size() > 0) {
				SubmissionResults results = doSubmission(session, editRequest);
				testEditedResults(results);
				results.getEncounterCreated();
				results.getPatient();
			}
		}
		
	}
	
	private String formatObsValueDate(Date date) {
		if (date == null) {
			return null;
		}
		String valueAsString = TestUtil.valueAsStringHelper(date);
		//The date format used by Obs.getValueAsString changed in 1.9.9
		if (ModuleUtil.compareVersion(OpenmrsConstants.OPENMRS_VERSION_SHORT, "1.9.9") > -1) {
			valueAsString = new SimpleDateFormat("yyyy-MM-dd").format(date);
		}
		return valueAsString;
	}
	
	private MockHttpServletRequest createEditRequest(String html, HttpSession httpSession) {
		MockHttpServletRequest ret = new MockHttpServletRequest();
		ret.setSession(httpSession);
		
		// used for input and for select option
		Pattern forValue = Pattern.compile("value=\"(.*?)\"");
		
		// <input ... name="something" ...>
		{
			Pattern forInput = Pattern.compile("<input.*?name=\"(.*?)\".*?>");
			Matcher matcher = forInput.matcher(html);
			while (matcher.find()) {
				String element = matcher.group();
				String name = matcher.group(1);
				Matcher lookForValue = forValue.matcher(element);
				if (lookForValue.find()) {
					String value = lookForValue.group(1);
					ret.addParameter(name, value);
				}
			}
		}
		
		// <textarea ... name="something" ...>value</textarea>
		{
			Pattern forTextarea = Pattern.compile("<textarea.*?name=\"(.*?)\".*?>(.*?)</textarea>");
			Matcher matcher = forTextarea.matcher(html);
			while (matcher.find()) {
				String name = matcher.group(1);
				String value = matcher.group(2);
				ret.addParameter(name, value);
			}
		}
		
		// <select ... name="something" ...>(options)</select> (DOTALL makes . match line terminator too)
		{
			Pattern forSelect = Pattern.compile("<select.*?name=\"(.*?)\".*?>.*?(<option[^>]*selected[^>]*>).*?</select>",
			    Pattern.DOTALL);
			Matcher matcher = forSelect.matcher(html);
			while (matcher.find()) {
				String name = matcher.group(1);
				String selectedOption = matcher.group(2);
				Matcher lookForValue = forValue.matcher(selectedOption);
				if (lookForValue.find()) {
					String value = lookForValue.group(1);
					ret.addParameter(name, value);
				} else {
					ret.addParameter(name, "");
				}
			}
		}
		
		// setupDatePicker(jsDateFormat, jsLocale, displaySelector, '#something', '2012-01-30')
		{
			Pattern forDatePicker = Pattern.compile("setupDatePicker\\(.*?, .*?, .*?, '#(.+?)', '(.+?)'\\)");
			Matcher matcher = forDatePicker.matcher(html);
			while (matcher.find()) {
				String name = matcher.group(1);
				String value = matcher.group(2);
				ret.addParameter(name, value);
			}
		}
		
		return ret;
	}
	
	/**
	 * Override this if you need to load your form's xml from somewhere other than the standard location
	 * defined by {@link RegressionTestHelper#XML_DATASET_PATH}. For example you should override this
	 * for any tests in modules that depend on HTML Form Entry.
	 */
	protected String getXmlDatasetPath() {
		return RegressionTestHelper.XML_DATASET_PATH;
	}
	
	/**
	 * Override this if you want to populate the FormEntrySession with extra attributes. This will be
	 * applied for Enter, Edit, and View.
	 * 
	 * @return map of attributes to be set on the FormEntrySession
	 */
	public Map<String, Object> getFormEntrySessionAttributes() {
		return null;
	}
	
	private FormEntrySession setupFormEntrySession(Patient patient, String filename) throws Exception {
		String xml = loadXmlFromFile(getXmlDatasetPath() + filename + ".xml");
		
		HtmlForm fakeForm = new HtmlForm();
		fakeForm.setXmlData(xml);
		fakeForm.setForm(new Form(1));
		fakeForm.getForm().setEncounterType(new EncounterType(1));
		FormEntrySession session = new FormEntrySession(patient, null, FormEntryContext.Mode.ENTER, fakeForm,
		        new MockHttpSession());
		session.setAttributes(getFormEntrySessionAttributes());
		session.getHtmlToDisplay();
		return session;
	}
	
	private FormEntrySession setupFormViewSession(Patient patient, Encounter encounter, String filename) throws Exception {
		String xml = loadXmlFromFile(getXmlDatasetPath() + filename + ".xml");
		
		HtmlForm fakeForm = new HtmlForm();
		fakeForm.setXmlData(xml);
		fakeForm.setForm(new Form(1));
		fakeForm.getForm().setEncounterType(new EncounterType(1));
		FormEntrySession session = new FormEntrySession(patient, encounter, FormEntryContext.Mode.VIEW, fakeForm,
		        new MockHttpSession());
		session.setAttributes(getFormEntrySessionAttributes());
		session.getHtmlToDisplay();
		return session;
	}
	
	private FormEntrySession setupFormEditSession(Patient patient, Encounter encounter, String filename) throws Exception {
		String xml = loadXmlFromFile(getXmlDatasetPath() + filename + ".xml");
		
		HtmlForm fakeForm = new HtmlForm();
		fakeForm.setXmlData(xml);
		fakeForm.setForm(new Form(1));
		fakeForm.getForm().setEncounterType(new EncounterType(1));
		FormEntrySession session = new FormEntrySession(patient, encounter, FormEntryContext.Mode.EDIT, fakeForm,
		        new MockHttpSession());
		session.setAttributes(getFormEntrySessionAttributes());
		session.getHtmlToDisplay();
		return session;
	}
	
	private String loadXmlFromFile(String filename) throws Exception {
		InputStream fileInInputStreamFormat = null;
		
		// try to load the file if its a straight up path to the file or
		// if its a classpath path to the file
		if (new File(filename).exists()) {
			fileInInputStreamFormat = new FileInputStream(filename);
		} else {
			fileInInputStreamFormat = getClass().getClassLoader().getResourceAsStream(filename);
			if (fileInInputStreamFormat == null)
				throw new FileNotFoundException("Unable to find '" + filename + "' in the classpath");
		}
		StringBuilder sb = new StringBuilder();
		BufferedReader r = new BufferedReader(new InputStreamReader(fileInInputStreamFormat, Charset.forName("UTF-8")));
		while (true) {
			String line = r.readLine();
			if (line == null)
				break;
			sb.append(line).append("\n");
		}
		return sb.toString();
	}
	
	public String dateAsString(Date date) {
		return new SimpleDateFormat("yyyy-MM-dd").format(date);
	}
	
	public Date ymdToDate(String dateString) {
		try {
			return new SimpleDateFormat("yyyy-MM-dd").parse(dateString);
		}
		catch (ParseException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	public String dateTodayAsString() {
		return dateAsString(new Date());
	}
	
	/**
	 * Finds the name of the first widget after each of the given labels. I.e. the first name="w#".
	 */
	private Map<String, String> getLabeledWidgets(String html, String... labels) {
		Map<String, String> ret = new HashMap<String, String>();
		for (String label : labels) {
			int toSkip = 0;
			// something like EncounterAndRole!!1 means the *second* widget after "EncounterAndRole"
			String origLabel = label;
			if (label.indexOf("!!") > 0) {
				String[] temp = label.split("!!");
				toSkip = Integer.valueOf(temp[1]);
				label = temp[0];
			}
			int index = html.indexOf(label);
			if (index < 0)
				continue;
			try {
				for (int i = 0; i < toSkip + 1; ++i) {
					index = html.indexOf("name=\"w", index);
					index = html.indexOf('"', index) + 1;
				}
				String val = html.substring(index, html.indexOf('"', index + 1));
				ret.put(origLabel, val);
			}
			catch (Exception ex) {
				// do nothing
			}
		}
		return ret;
	}
	
	private SubmissionResults doSubmission(FormEntrySession session, HttpServletRequest request) throws Exception {
		SubmissionResults results = new SubmissionResults();
		session.prepareForSubmit();
		List<FormSubmissionError> validationErrors = session.getSubmissionController()
		        .validateSubmission(session.getContext(), request);
		if (validationErrors != null && validationErrors.size() > 0) {
			results.setValidationErrors(validationErrors);
			return results;
		}
		session.getSubmissionController().handleFormSubmission(session, request);
		
		if (session.getContext().getMode() == Mode.ENTER && session.hasEncouterTag()
		        && (session.getSubmissionActions().getEncountersToCreate() == null
		                || session.getSubmissionActions().getEncountersToCreate().size() == 0))
			throw new IllegalArgumentException("This form is not going to create an encounter");
		Context.getService(HtmlFormEntryService.class).applyActions(session);
		results.setPatient(session.getPatient());
		results.setEncounterCreated(session.getEncounter());
		results.setFormEntrySession(session);
		return results;
	}
	
	public class SubmissionResults {
		
		private List<FormSubmissionError> validationErrors;
		
		private Patient patient;
		
		private Encounter encounterCreated;
		
		private FormEntrySession formEntrySession;
		
		public void setFormEntrySession(FormEntrySession formEntrySession) {
			this.formEntrySession = formEntrySession;
		}
		
		public FormEntrySession getFormEntrySession() {
			return formEntrySession;
		}
		
		public void assertNoEncounterCreated() {
			Assert.assertNull(encounterCreated);
		}
		
		public void assertEncounterCreated() {
			Assert.assertNotNull(encounterCreated);
		}
		
		public void assertEncounterEdited() {
			Assert.assertNotNull("No encounter found", encounterCreated);
			Assert.assertNotNull("Encounter date changed not set on edit", encounterCreated.getDateChanged());
		}
		
		public void assertEncounterVoided() {
			Assert.assertTrue("Encounter not voided", encounterCreated.isVoided());
		}
		
		public void assertEncounterNotVoided() {
			Assert.assertFalse("Encounter voided", encounterCreated.isVoided());
		}
		
		public void assertNoErrors() {
			Assert.assertTrue("" + validationErrors, validationErrors == null || validationErrors.size() == 0);
		}
		
		public void assertErrors() {
			Assert.assertTrue(validationErrors != null && validationErrors.size() > 0);
		}
		
		public void assertErrors(int numberOfErrors) {
			Assert.assertTrue(validationErrors != null && validationErrors.size() == numberOfErrors);
		}
		
		public void printErrors() {
			if (validationErrors == null || validationErrors.size() == 0) {
				System.out.println("No Errors");
			} else {
				for (FormSubmissionError error : validationErrors)
					System.out.println(error.getId() + " -> " + error.getError());
			}
		}
		
		public void print() {
			printErrors();
			printEncounterCreated();
		}
		
		public void printEncounterCreated() {
			if (encounterCreated == null) {
				System.out.println("No encounter created");
			} else {
				System.out.println("=== Encounter created ===");
				System.out.println(
				    "Created: " + encounterCreated.getDateCreated() + "  Edited: " + encounterCreated.getDateChanged());
				System.out.println("Date: " + encounterCreated.getEncounterDatetime());
				System.out.println("Location: " + encounterCreated.getLocation().getName());
				System.out.println("Provider: " + getProvider(encounterCreated).getPersonName());
				System.out.println("    (obs)");
				Collection<Obs> obs = encounterCreated.getAllObs(false);
				if (obs == null) {
					System.out.println("None");
				} else {
					for (Obs o : obs) {
						System.out.println(o.getConcept().getName() + " -> " + o.getValueAsString(Context.getLocale()));
					}
				}
			}
		}
		
		public List<FormSubmissionError> getValidationErrors() {
			return validationErrors;
		}
		
		public void setValidationErrors(List<FormSubmissionError> validationErrors) {
			this.validationErrors = validationErrors;
		}
		
		public Encounter getEncounterCreated() {
			return encounterCreated;
		}
		
		public void setEncounterCreated(Encounter encounterCreated) {
			this.encounterCreated = encounterCreated;
		}
		
		public Patient getPatient() {
			return patient;
		}
		
		public void setPatient(Patient patient) {
			this.patient = patient;
		}
		
		/**
		 * Fails if there is a patient (one was initially selected or one was created)
		 */
		public void assertNoPatient() {
			Assert.assertNull(patient);
		}
		
		/**
		 * Fails if there is no patient (none was initially selected and none was created), or the patient
		 * doesn't have a patientId assigned.
		 */
		public void assertPatient() {
			Assert.assertNotNull(patient);
			Assert.assertNotNull(patient.getPatientId());
		}
		
		/**
		 * Fails if there is no provider with an assigned id associated with the encounter
		 */
		public void assertProvider() {
			assertEncounterCreated();
			Assert.assertNotNull(getProvider(getEncounterCreated()));
			Assert.assertNotNull(getProvider(getEncounterCreated()).getPersonId());
		}
		
		public void assertNoProvider() {
			assertEncounterCreated();
			Assert.assertNull(getProvider(getEncounterCreated()));
		}
		
		/**
		 * Fails if there is no provider or if the provider id does not match the expected id
		 */
		public void assertProvider(Integer expectedProviderId) {
			assertProvider();
			Assert.assertEquals(expectedProviderId, getProvider(getEncounterCreated()).getPersonId());
		}
		
		/**
		 * Fails if there is no location with an assigned id associated with the encounter
		 */
		public void assertLocation() {
			assertEncounterCreated();
			Assert.assertNotNull(getEncounterCreated().getLocation());
			Assert.assertNotNull(getEncounterCreated().getLocation().getLocationId());
		}
		
		public void assertNoLocation() {
			assertEncounterCreated();
			Assert.assertNull(getEncounterCreated().getLocation());
		}
		
		/**
		 * Fails if there is no location or if the location id does not match the expected location id
		 */
		public void assertLocation(Integer expectedLocationId) {
			assertLocation();
			Assert.assertEquals(expectedLocationId, getEncounterCreated().getLocation().getLocationId());
		}
		
		public void assertEncounterType() {
			assertEncounterCreated();
			Assert.assertNotNull(getEncounterCreated().getEncounterType());
			Assert.assertNotNull(getEncounterCreated().getEncounterType().getEncounterTypeId());
		}
		
		public void assertEncounterType(Integer expectedEncounterTypeId) {
			assertEncounterType();
			Assert.assertEquals(expectedEncounterTypeId, getEncounterCreated().getEncounterType().getEncounterTypeId());
		}
		
		public void assertEncounterDatetime() {
			assertEncounterCreated();
			Assert.assertNotNull(getEncounterCreated().getEncounterDatetime());
		}
		
		public void assertEncounterDatetime(Date expectedEncounterDate) {
			assertEncounterDatetime();
			Assert.assertEquals(expectedEncounterDate, getEncounterCreated().getEncounterDatetime());
		}
		
		/**
		 * Fails if the number of obs in encounterCreated is not 'expected'
		 * 
		 * @param expected
		 */
		public void assertObsCreatedCount(int expected) {
			int found = getObsCreatedCount();
			Assert.assertEquals("Expected to create " + expected + " obs but got " + found, expected, found);
		}
		
		/**
		 * Fails if the number of obs groups in encounterCreated is not 'expected'
		 * 
		 * @param expected
		 */
		public void assertObsGroupCreatedCount(int expected) {
			int found = getObsGroupCreatedCount();
			Assert.assertEquals("Expected to create " + expected + " obs groups but got " + found, expected, found);
		}
		
		/**
		 * Fails if the number of obs leaves (i.e. obs that aren't groups) in encounterCreated is not
		 * 'expected'
		 * 
		 * @param expected
		 */
		public void assertObsLeafCreatedCount(int expected) {
			int found = getObsLeafCreatedCount();
			Assert.assertEquals("Expected to create " + expected + " non-group obs but got " + found, expected, found);
		}
		
		/**
		 * Fails if any Obs with the given Concept Id doesn't have previous_version set and a matching
		 * voided obs exists in the encounter, or if no non-void obs with Concept Id is found
		 * 
		 * @param conceptId
		 */
		public void assertPreviousVersion(int conceptId) {
			
			Assert.assertNotNull(encounterCreated);
			Collection<Obs> temp = encounterCreated.getAllObs(true);
			Assert.assertNotNull(temp);
			
			Obs found = null;
			
			for (Obs curObs : temp) {
				if (curObs.getConcept().getId() == conceptId && !curObs.getVoided()) {
					//just keep track that an non-void Obs 
					//with the provided concept Id was found in the encounter
					
					found = curObs;
					Assert.assertNotNull(
					    "All Obs with provided Concept Id should have been modified and have a previous version set",
					    curObs.getPreviousVersion());
					
					Assert.assertTrue("Previous version should be in encounter", temp.contains(curObs.getPreviousVersion()));
					
					Assert.assertTrue("Previous version should be voided", curObs.getPreviousVersion().getVoided());
					
				}
			}
			
			Assert.assertNotNull("No Obs with provided Concept Id was found", found);
			
		}
		
		/**
		 * Fails if the Obs specified by the given Obs Id does not have previous_version set or if matching
		 * voided Obs does not exist in encounter
		 * 
		 * @param obsId
		 */
		public void assertPreviousVersionById(int obsId) {
			
			Assert.assertNotNull(encounterCreated);
			Collection<Obs> temp = encounterCreated.getAllObs(true);
			Assert.assertNotNull(temp);
			
			Obs found = null;
			
			for (Obs curObs : temp) {
				if (curObs.getId() == obsId && !curObs.getVoided()) {
					found = curObs;
				}
			}
			
			Assert.assertNotNull("Obs with provided Obs id not found", found);
			Assert.assertNotNull("Obs with provided Obs id should have a previous version set", found.getPreviousVersion());
			Assert.assertTrue("Previous version should be in encounter", temp.contains(found.getPreviousVersion()));
			Assert.assertTrue("Previous version should be voided", found.getPreviousVersion().getVoided());
		}
		
		/**
		 * Fails if any Obs in the encounter does not have a previous_version set or a matching voided obs
		 * does not exist in encounter
		 */
		public void assertPreviousVersions() {
			
			Assert.assertNotNull("Encounter was not created", encounterCreated);
			Collection<Obs> temp = encounterCreated.getAllObs(true);
			Assert.assertNotNull("No observations were created", temp);
			
			for (Obs curObs : temp) {
				if (!curObs.getVoided()) {
					Assert.assertTrue("Previous version on new Obs Id#" + curObs.getId() + " should reference a voided Obs",
					    temp.contains(curObs.getPreviousVersion()));
				}
			}
		}
		
		/**
		 * @return the number of obs in encounterCreated (0 if no encounter was created)
		 */
		public int getObsCreatedCount() {
			if (encounterCreated == null)
				return 0;
			Collection<Obs> temp = encounterCreated.getAllObs();
			if (temp == null)
				return 0;
			return temp.size();
		}
		
		/**
		 * @return the number of obs groups in encounterCreated (0 if no encounter was created)
		 */
		public int getObsGroupCreatedCount() {
			if (encounterCreated == null)
				return 0;
			Collection<Obs> temp = encounterCreated.getAllObs();
			if (temp == null)
				return 0;
			int count = 0;
			for (Obs o : temp) {
				if (o.isObsGrouping())
					++count;
			}
			return count;
		}
		
		/**
		 * @return the number of non-group obs in encounterCreated (0 if no encounter was created)
		 */
		public int getObsLeafCreatedCount() {
			if (encounterCreated == null)
				return 0;
			Collection<Obs> temp = encounterCreated.getObs();
			if (temp == null)
				return 0;
			return temp.size();
		}
		
		private void assertObsExists(boolean lookForVoided, int conceptId, Object value) {
			// quick checks
			Assert.assertNotNull(encounterCreated);
			Collection<Obs> temp = encounterCreated.getAllObs(lookForVoided);
			Assert.assertNotNull(temp);
			
			String valueAsString = null;
			if (value instanceof Date) {
				valueAsString = formatObsValueDate((Date) value);
			} else {
				valueAsString = TestUtil.valueAsStringHelper(value);
			}
			for (Obs obs : temp) {
				if (lookForVoided && !obs.isVoided())
					continue;
				if (obs.getConcept().getConceptId() == conceptId) {
					if (valueAsString == null) {
						return;
					}
					String valueToCompare = obs.getValueAsString(Context.getLocale());
					if (valueAsString.equals(valueToCompare)) {
						return;
					}
					if (obs.getValueDatetime() != null) {
						if (valueAsString.equals(formatObsValueDate(obs.getValueDatetime()))) {
							return;
						}
						;
					}
				}
			}
			Assert.fail("Could not find obs with conceptId " + conceptId + " and value " + valueAsString);
		}
		
		/**
		 * Fails if encounterCreated doesn't have an obs with the given conceptId and value
		 * 
		 * @param conceptId
		 * @param value may be null
		 */
		public void assertObsCreated(int conceptId, Object value) {
			assertObsExists(false, conceptId, value);
		}
		
		/**
		 * Fails if encounterCreated doesn't have a voided obs with the given conceptId and value
		 * 
		 * @param conceptId
		 * @param value
		 */
		public void assertObsVoided(int conceptId, Object value) {
			assertObsExists(true, conceptId, value);
		}
		
		/**
		 * Fails if there isn't an obs group with these exact characteristics
		 * 
		 * @param groupingConceptId the concept id of the grouping obs
		 * @param conceptIdsAndValues these parameters must be given in pairs, the first element of which is
		 *            the conceptId of a child obs (Integer) and the second element of which is the value of
		 *            the child obs
		 */
		public void assertObsGroupCreated(int groupingConceptId, Object... conceptIdsAndValues) {
			// quick checks
			Assert.assertNotNull(encounterCreated);
			Collection<Obs> temp = encounterCreated.getAllObs();
			Assert.assertNotNull(temp);
			
			List<ObsValue> expected = new ArrayList<ObsValue>();
			for (int i = 0; i < conceptIdsAndValues.length; i += 2) {
				int conceptId = (Integer) conceptIdsAndValues[i];
				Object value = conceptIdsAndValues[i + 1];
				expected.add(new ObsValue(conceptId, value));
			}
			
			for (Obs o : temp) {
				if (o.getConcept().getConceptId() == groupingConceptId) {
					if (o.getValueCoded() != null || o.getValueComplex() != null || o.getValueDatetime() != null
					        || o.getValueDrug() != null || o.getValueNumeric() != null || o.getValueText() != null) {
						Assert.fail(
						    "Obs group with groupingConceptId " + groupingConceptId + " should has a non-null value");
					}
					if (TestUtil.isMatchingObsGroup(o, expected)) {
						return;
					}
				}
			}
			Assert.fail("Cannot find an obs group matching " + expected);
		}
		
	}
	
	public class ObsValue {
		
		public Integer conceptId; // required
		
		public Object value; // can be null
		
		public ObsValue(Integer cId, Object val) {
			conceptId = cId;
			value = val;
		}
		
		@Override
		public String toString() {
			return conceptId + "->" + value;
		}
		
		public boolean matches(Obs obs) {
			
			if (!obs.getConcept().getConceptId().equals(conceptId)) {
				return false;
			}
			String valueAsString = null;
			if (value instanceof Date) {
				valueAsString = formatObsValueDate((Date) value);
			} else {
				valueAsString = TestUtil.valueAsStringHelper(value);
			}
			return OpenmrsUtil.nullSafeEquals(valueAsString, obs.getValueAsString(Context.getLocale()));
		}
	}
	
	// helper workaround method now that as of platform 2.x, Encounter does not have a getProvider method
	private Person getProvider(Encounter enc) {
		if (enc.getEncounterProviders() == null || enc.getEncounterProviders().isEmpty()) {
			return null;
		} else {
			for (EncounterProvider encounterProvider : enc.getEncounterProviders()) {
				// Return the first non-voided provider associated with a person in the list
				if (!encounterProvider.isVoided() && encounterProvider.getProvider().getPerson() != null) {
					return encounterProvider.getProvider().getPerson();
				}
			}
		}
		return null;
	}
}
