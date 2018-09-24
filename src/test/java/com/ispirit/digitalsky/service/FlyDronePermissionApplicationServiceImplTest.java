package com.ispirit.digitalsky.service;

import com.ispirit.digitalsky.SecurityContextHelper;
import com.ispirit.digitalsky.document.FlyDronePermissionApplication;
import com.ispirit.digitalsky.document.LatLong;
import com.ispirit.digitalsky.domain.*;
import com.ispirit.digitalsky.dto.Errors;
import com.ispirit.digitalsky.exception.ApplicationNotFoundException;
import com.ispirit.digitalsky.exception.ApplicationNotInSubmittedStatusException;
import com.ispirit.digitalsky.exception.StorageFileNotFoundException;
import com.ispirit.digitalsky.exception.ValidationException;
import com.ispirit.digitalsky.repository.FlyDronePermissionApplicationRepository;
import com.ispirit.digitalsky.repository.storage.StorageService;
import com.ispirit.digitalsky.service.api.AirspaceCategoryService;
import com.ispirit.digitalsky.service.api.DigitalSignService;
import com.ispirit.digitalsky.service.api.OperatorDroneService;
import freemarker.template.Configuration;
import org.apache.commons.io.IOUtils;
import org.geojson.FeatureCollection;
import org.geojson.GeoJsonObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.core.io.Resource;
import org.springframework.security.util.InMemoryResource;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Date;

import java.util.HashMap;
import java.util.List;

import static com.ispirit.digitalsky.service.FlyDronePermissionApplicationServiceImpl.PERMISSION_ARTIFACT_XML;
import static java.util.Arrays.asList;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

public class FlyDronePermissionApplicationServiceImplTest {

    private FlyDronePermissionApplicationRepository repository;
    private StorageService storageService;
    private FlyDronePermissionApplicationServiceImpl service;

    private AirspaceCategoryService airspaceCategoryService;
    private UserPrincipal userPrincipal;
    private DigitalSignService digitalSignService;
    private OperatorDroneService operatorDroneService;
    private Configuration freemarkerConfiguration;

    @Before
    public void setUp() throws Exception {
        repository = mock(FlyDronePermissionApplicationRepository.class);
        storageService = mock(StorageService.class);
        airspaceCategoryService = mock(AirspaceCategoryService.class);
        digitalSignService = mock(DigitalSignService.class);
        operatorDroneService = mock(OperatorDroneService.class);
        freemarkerConfiguration = mock(Configuration.class);
        service = spy(new FlyDronePermissionApplicationServiceImpl(repository, storageService, airspaceCategoryService, digitalSignService, operatorDroneService, freemarkerConfiguration));
        userPrincipal = SecurityContextHelper.setUserSecurityContext();
    }

    @Test
    public void shouldCreateApplication() throws Exception {
        //given
        FlyDronePermissionApplication application = new FlyDronePermissionApplication();
        application.setPilotId("1");
        application.setDroneId(1);
        application.setFlyArea(asList(new LatLong(1, 1), new LatLong(2, 2)));
        doNothing().when(service).validateFlyArea(application);
        when(operatorDroneService.find(application.getDroneId())).thenReturn(new OperatorDrone());
        //when
        service.createApplication(application);

        //then
        ArgumentCaptor<FlyDronePermissionApplication> argumentCaptor = ArgumentCaptor.forClass(FlyDronePermissionApplication.class);
        verify(repository).insert(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue().getCreatedDate(), notNullValue());
        assertThat(argumentCaptor.getValue().getLastModifiedDate(), notNullValue());
        assertThat(argumentCaptor.getValue().getApplicantId(), is(userPrincipal.getId()));
        assertThat(argumentCaptor.getValue().getPilotId(), is(application.getPilotId()));
        assertThat(argumentCaptor.getValue().getFlyArea(), is(application.getFlyArea()));
    }

    @Test
    public void shouldHandleSubmitCreateApplicationSubmitted() throws Exception {
        //given
        FlyDronePermissionApplication application = new FlyDronePermissionApplication();
        application.setPilotId("1");
        application.setDroneId(1);
        application.setFlyArea(asList(new LatLong(1, 1), new LatLong(2, 2)));
        application.setStatus(ApplicationStatus.SUBMITTED);
        doNothing().when(service).validateFlyArea(application);
        doNothing().when(service).handleSubmit(application);
        doNothing().when(service).generatePermissionArtifact(any());
        when(operatorDroneService.find(application.getDroneId())).thenReturn(new OperatorDrone());
        //when
        service.createApplication(application);

        //then
        ArgumentCaptor<FlyDronePermissionApplication> argumentCaptor = ArgumentCaptor.forClass(FlyDronePermissionApplication.class);
        verify(repository).insert(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue().getSubmittedDate(), notNullValue());
        verify(service).handleSubmit(application);
        verify(service).generatePermissionArtifact(any());
    }

    @Test
    public void shouldUpdateApplication() throws Exception {
        //given
        LocalDateTime dateTime = LocalDateTime.of(2018, Month.AUGUST, 12, 0, 0);
        FlyDronePermissionApplication applicationPayload = new FlyDronePermissionApplication();
        applicationPayload.setPilotId("2");
        applicationPayload.setFlyArea(asList(new LatLong(1, 1), new LatLong(2, 2)));
        applicationPayload.setEndDateTime(LocalDateTime.of(2018, Month.AUGUST,21,0,0));
        applicationPayload.setEndDateTime(LocalDateTime.of(2018, Month.AUGUST,22,0,0));
        applicationPayload.setPayloadWeightInKg(2.5);
        applicationPayload.setPayloadDetails("food");
        applicationPayload.setFlightPurpose("parcel");

        FlyDronePermissionApplication application = new FlyDronePermissionApplication();
        application.setId("1");
        application.setPilotId("1");
        application.setFlyArea(asList(new LatLong(1, 1), new LatLong(2, 2)));
        application.setCreatedDate(Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant()));
        application.setLastModifiedDate(Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant()));
        application.setApplicantId(1);

        when(repository.findById("1")).thenReturn(application);
        doNothing().when(service).validateFlyArea(application);
        //when
        service.updateApplication("1", applicationPayload);

        //then
        ArgumentCaptor<FlyDronePermissionApplication> argumentCaptor = ArgumentCaptor.forClass(FlyDronePermissionApplication.class);
        verify(repository).save(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue().getCreatedDate(), is(application.getCreatedDate()));
        assertThat(argumentCaptor.getValue().getLastModifiedDate(), notNullValue());
        assertThat(argumentCaptor.getValue().getApplicantId(), is(application.getApplicantId()));
        assertThat(argumentCaptor.getValue().getPilotId(), is(applicationPayload.getPilotId()));
        assertThat(argumentCaptor.getValue().getFlyArea(), is(applicationPayload.getFlyArea()));
        assertThat(argumentCaptor.getValue().getStartDateTime(), is(applicationPayload.getStartDateTime()));
        assertThat(argumentCaptor.getValue().getEndDateTime(), is(applicationPayload.getEndDateTime()));
        assertThat(argumentCaptor.getValue().getPayloadWeightInKg(), is(applicationPayload.getPayloadWeightInKg()));
        assertThat(argumentCaptor.getValue().getPayloadDetails(), is(applicationPayload.getPayloadDetails()));
        assertThat(argumentCaptor.getValue().getFlightPurpose(), is(applicationPayload.getFlightPurpose()));
    }

    @Test
    public void shouldMarkApplicationApprovedWhenFlyAreaIsGoodDuringUpdate() throws Exception{

        //given
        LocalDateTime dateTime = LocalDateTime.of(2018, Month.AUGUST, 12, 0, 0);
        FlyDronePermissionApplication applicationPayload = new FlyDronePermissionApplication();
        applicationPayload.setStatus(ApplicationStatus.SUBMITTED);
        applicationPayload.setPilotId("2");
        applicationPayload.setFlyArea(asList(new LatLong(1, 1), new LatLong(2, 2)));
        applicationPayload.setEndDateTime(LocalDateTime.of(2018, Month.AUGUST,21,0,0));
        applicationPayload.setEndDateTime(LocalDateTime.of(2018, Month.AUGUST,22,0,0));
        applicationPayload.setPayloadWeightInKg(2.5);
        applicationPayload.setPayloadDetails("food");
        applicationPayload.setFlightPurpose("parcel");

        FlyDronePermissionApplication application = new FlyDronePermissionApplication();
        application.setId("1");
        application.setPilotId("1");
        application.setFlyArea(asList(new LatLong(1, 1), new LatLong(2, 2)));
        application.setCreatedDate(Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant()));
        application.setLastModifiedDate(Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant()));
        application.setApplicantId(1);

        HashMap<AirspaceCategory.Type, GeoJsonObject> airspaceCategoryMap = new HashMap<>();
        airspaceCategoryMap.put(AirspaceCategory.Type.AMBER,new FeatureCollection());

        when(repository.findById("1")).thenReturn(application);
        when(airspaceCategoryService.findGeoJsonMapByType()).thenReturn(airspaceCategoryMap);
        doNothing().when(service).validateFlyArea(application);
        doReturn(false).when(service).isFlyAreaIntersects(anyString(), eq(applicationPayload.getFlyArea()));
        doNothing().when(service).generatePermissionArtifact(application);

        //when
        service.updateApplication("1", applicationPayload);

        //then
        ArgumentCaptor<FlyDronePermissionApplication> argumentCaptor = ArgumentCaptor.forClass(FlyDronePermissionApplication.class);
        verify(repository).save(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue().getSubmittedDate(), notNullValue());
        assertThat(argumentCaptor.getValue().getStatus(), is(ApplicationStatus.APPROVED));
        verify(service).generatePermissionArtifact(application);

    }

    @Test
    public void shouldThrowExceptionIfApplicationNotFoundDuringUpdate() throws Exception {

        //when
        try {
            service.updateApplication("1", new FlyDronePermissionApplication());
            fail("should have thrown ApplicationNotFoundException");
        } catch (ApplicationNotFoundException e) {

        }
    }

    @Test
    public void shouldMarkApplicationAsApproved() throws Exception {
        //given
        ApproveRequestBody approveRequestBody = new ApproveRequestBody();
        approveRequestBody.setApplicationFormId("1");
        approveRequestBody.setStatus(ApplicationStatus.APPROVED);
        approveRequestBody.setComments("comments");

        FlyDronePermissionApplication application = new FlyDronePermissionApplication();
        application.setId("1");
        application.setPilotId("1");
        application.setFlyArea(asList(new LatLong(1, 1), new LatLong(2, 2)));
        application.setApplicantId(1);
        application.setStatus(ApplicationStatus.SUBMITTED);
        when(repository.findById("1")).thenReturn(application);

        //when
        service.approveApplication(approveRequestBody);

        //then
        ArgumentCaptor<FlyDronePermissionApplication> argumentCaptor = ArgumentCaptor.forClass(FlyDronePermissionApplication.class);
        verify(repository).save(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue().getApprover(), is(userPrincipal.getUsername()));
        assertThat(argumentCaptor.getValue().getApproverId(), is(userPrincipal.getId()));
        assertThat(argumentCaptor.getValue().getApprovedDate(), notNullValue());
        assertThat(argumentCaptor.getValue().getApproverComments(), is(approveRequestBody.getComments()));
        assertThat(argumentCaptor.getValue().getStatus(), is(ApplicationStatus.APPROVED));
    }

    @Test
    public void shouldThrowExceptionIfApplicationNotFoundDuringApprove() throws Exception {
        //given
        ApproveRequestBody approveRequestBody = new ApproveRequestBody();
        approveRequestBody.setApplicationFormId("1");
        approveRequestBody.setStatus(ApplicationStatus.APPROVED);
        approveRequestBody.setComments("comments");
        //when
        try {
            service.approveApplication(approveRequestBody);
            fail("should have thrown ApplicationNotFoundException");
        } catch (ApplicationNotFoundException e) {

        }
    }

    @Test
    public void shouldNotApproveApplicationIfNotSubmitted() throws Exception {
        //given
        ApproveRequestBody approveRequestBody = new ApproveRequestBody();
        approveRequestBody.setApplicationFormId("1");
        approveRequestBody.setStatus(ApplicationStatus.APPROVED);
        approveRequestBody.setComments("comments");

        FlyDronePermissionApplication application = new FlyDronePermissionApplication();
        application.setStatus(ApplicationStatus.DRAFT);
        when(repository.findById("1")).thenReturn(application);

        //when
        try {
            service.approveApplication(approveRequestBody);
            fail("should have thrown ApplicationNotFoundException");
        } catch (ApplicationNotInSubmittedStatusException e) {

        }
    }

    @Test
    public void shouldGetApplicationById() throws Exception {
        //given
        FlyDronePermissionApplication application = new FlyDronePermissionApplication();
        when(repository.findById("1")).thenReturn(application);

        //when
        FlyDronePermissionApplication result = service.get("1");

        //then
        verify(repository).findById("1");
        assertThat(result, is(application));
    }

    @Test
    public void shouldGetAllApplicationsForGivenDrone() throws Exception {
        FlyDronePermissionApplication applicationOne = application(LocalDateTime.of(2018, Month.AUGUST, 29, 0, 0, 0));
        FlyDronePermissionApplication applicationTwo = application(LocalDateTime.of(2018, Month.AUGUST, 30, 0, 0, 0));
        FlyDronePermissionApplication applicationThree = application(LocalDateTime.of(2018, Month.AUGUST, 31, 0, 0, 0));

        when(repository.findByDroneId(1L)).thenReturn(asList(applicationOne, applicationTwo, applicationThree));

        //when
        Collection<FlyDronePermissionApplication> applications = service.getApplicationsOfDrone(1L);

        assertThat(applications, is(asList(applicationThree, applicationTwo, applicationOne)));

    }

    @Test
    public void shouldGetAllApplications() throws Exception {
        FlyDronePermissionApplication applicationOne = application(LocalDateTime.of(2018, Month.AUGUST, 29, 0, 0, 0));
        FlyDronePermissionApplication applicationTwo = application(LocalDateTime.of(2018, Month.AUGUST, 30, 0, 0, 0));
        FlyDronePermissionApplication applicationThree = application(LocalDateTime.of(2018, Month.AUGUST, 31, 0, 0, 0));

        when(repository.findAll()).thenReturn(asList(applicationOne, applicationTwo, applicationThree));

        //when
        Collection<FlyDronePermissionApplication> applications = service.getAllApplications();

        assertThat(applications, is(asList(applicationThree, applicationTwo, applicationOne)));

    }

    @Test
    public void shouldGetFile() throws Exception {

        //when
        service.getPermissionArtifact("1");

        //then
        verify(storageService).loadAsResource("1", PERMISSION_ARTIFACT_XML);
    }

    @Test
    public void shouldGetRegenerateArtifactIfNotFound() throws Exception {

        //when
        FlyDronePermissionApplication application = new FlyDronePermissionApplication();
        InMemoryResource resource = new InMemoryResource("");
        when(storageService.loadAsResource("1",PERMISSION_ARTIFACT_XML))
                .thenThrow(new StorageFileNotFoundException("")).thenReturn(resource);

        doReturn(application).when(service).get("1");
        doNothing().when(service).generatePermissionArtifact(application);

        //when
        Resource result = service.getPermissionArtifact("1");

        //then
        assertThat(result, is(resource));
        verify(service).generatePermissionArtifact(application);
    }

    @Test
    public void shouldFindFlyAreaWithinGreenZones() throws Exception {
        //given
        String greenZones = IOUtils.toString(this.getClass().getResourceAsStream("/geoJsonGreenZones.json"), "UTF-8");
        LatLong one = new LatLong(12.232654837013484, 75.87158203125);
        LatLong two = new LatLong(11.802834233547687, 76.168212890625);
        LatLong three = new LatLong(11.77057019562524, 76.761474609375);
        LatLong four = new LatLong(12.302435369557129, 77.003173828125);
        LatLong five = new LatLong(12.538477567560662, 76.4044189453125);
        LatLong six = new LatLong(12.232654837013484, 75.87158203125);

        //when
        try {
            service.validateFlyAreaWithin(greenZones, asList(one, two, three, four, five, six));
        } catch (ValidationException e) {
            fail("should not have thrown ValidationException");
        }
    }

    @Test
    public void shouldNotFindFlyAreaWithinGreenZones() throws Exception {
        //given
        String greenZones = IOUtils.toString(this.getClass().getResourceAsStream("/geoJsonGreenZones.json"), "UTF-8");
        LatLong one = new LatLong(11.630715737981486, 68.88427734374999);
        LatLong two = new LatLong(7.18810087117902, 68.70849609375);
        LatLong three = new LatLong(11.695272733029402, 77.89306640625);
        LatLong four = new LatLong(14.817370620155254, 77.58544921874999);
        LatLong five = new LatLong(11.630715737981486, 68.88427734374999);

        //when
        try {
            service.validateFlyAreaWithin(greenZones, asList(one, two, three, four, five));
            fail("should have thrown ValidationException");
        } catch (ValidationException e) {
        }
    }

    @Test
    public void shouldValidateIfFlyAreaWithinGreenZones() throws Exception {
        //given
        service = spy(new FlyDronePermissionApplicationServiceImpl(repository, storageService, airspaceCategoryService, digitalSignService, operatorDroneService, freemarkerConfiguration));
        LatLong one = new LatLong(11.630715737981486, 68.88427734374999);
        LatLong two = new LatLong(7.18810087117902, 68.70849609375);
        LatLong three = new LatLong(11.695272733029402, 77.89306640625);
        LatLong four = new LatLong(14.817370620155254, 77.58544921874999);
        LatLong five = new LatLong(11.630715737981486, 68.88427734374999);
        List<LatLong> flyArea = asList(one, two, three, four, five);

        doThrow(new ValidationException(new Errors())).when(service).validateFlyAreaWithin(anyString(), eq(flyArea));
        FlyDronePermissionApplication application = new FlyDronePermissionApplication();
        application.setFlyArea(flyArea);

        when(airspaceCategoryService.findGeoJsonMapByType()).thenReturn(new HashMap<>());


        //when
        try {
            service.validateFlyArea(application);
        } catch (ValidationException e) {
        }
        verify(service).validateFlyAreaWithin(anyString(), eq(flyArea));
    }

    @Test
    public void shouldValidateIfFlyAreaIntersectWithRedZones() throws Exception {
        //given
        service = spy(new FlyDronePermissionApplicationServiceImpl(repository, storageService, airspaceCategoryService, digitalSignService, operatorDroneService, freemarkerConfiguration));
        LatLong one = new LatLong(11.630715737981486, 68.88427734374999);
        LatLong two = new LatLong(7.18810087117902, 68.70849609375);
        LatLong three = new LatLong(11.695272733029402, 77.89306640625);
        LatLong four = new LatLong(14.817370620155254, 77.58544921874999);
        LatLong five = new LatLong(11.630715737981486, 68.88427734374999);
        List<LatLong> flyArea = asList(one, two, three, four, five);

        doNothing().when(service).validateFlyAreaWithin(anyString(), eq(flyArea));
        doThrow(new ValidationException(new Errors())).when(service).validateFlyAreaIntersectsRedZones(anyString(), eq(flyArea));
        FlyDronePermissionApplication application = new FlyDronePermissionApplication();
        application.setFlyArea(flyArea);

        when(airspaceCategoryService.findGeoJsonMapByType()).thenReturn(new HashMap<>());


        //when
        try {
            service.validateFlyArea(application);
        } catch (ValidationException e) {
        }
        verify(service).validateFlyAreaIntersectsRedZones(anyString(), eq(flyArea));
    }

    @Test
    public void handleSubmitShouldCheckIfFlyAreaIntersectWithAmberZones() throws Exception {
        //given
        service = spy(new FlyDronePermissionApplicationServiceImpl(repository, storageService, airspaceCategoryService, digitalSignService, operatorDroneService, freemarkerConfiguration));
        LatLong one = new LatLong(11.630715737981486, 68.88427734374999);
        LatLong two = new LatLong(7.18810087117902, 68.70849609375);
        LatLong three = new LatLong(11.695272733029402, 77.89306640625);
        LatLong four = new LatLong(14.817370620155254, 77.58544921874999);
        LatLong five = new LatLong(11.630715737981486, 68.88427734374999);
        List<LatLong> flyArea = asList(one, two, three, four, five);

        doReturn(true).when(service).isFlyAreaIntersects(anyString(),eq(flyArea));

        FlyDronePermissionApplication application = new FlyDronePermissionApplication();
        application.setFlyArea(flyArea);

        when(airspaceCategoryService.findGeoJsonMapByType()).thenReturn(new HashMap<>());


        //when
        try {
            service.handleSubmit(application);
        } catch (ValidationException e) {
        }
        verify(service).isFlyAreaIntersects(anyString(), eq(flyArea));
    }

    @Test
    public void shouldApproveApplicationAfterSubmit() throws Exception {
        //given
        FlyDronePermissionApplication application = new FlyDronePermissionApplication();
        service = spy(new FlyDronePermissionApplicationServiceImpl(repository, storageService, airspaceCategoryService, digitalSignService, operatorDroneService, freemarkerConfiguration));
        LatLong one = new LatLong(11.630715737981486, 68.88427734374999);
        LatLong two = new LatLong(7.18810087117902, 68.70849609375);
        LatLong three = new LatLong(11.695272733029402, 77.89306640625);
        LatLong four = new LatLong(14.817370620155254, 77.58544921874999);
        LatLong five = new LatLong(11.630715737981486, 68.88427734374999);
        List<LatLong> flyArea = asList(one, two, three, four, five);
        application.setFlyArea(flyArea);

        doReturn(false).when(service).isFlyAreaIntersects(anyString(),eq(flyArea));


        //when
        try {
            service.handleSubmit(application);
        } catch (ValidationException e) {
        }
        assertThat(application.getStatus(),is(ApplicationStatus.APPROVED));
        assertThat(application.getApproverId(),is(userPrincipal.getId()));
        assertThat(application.getApprovedDate(),notNullValue());
        assertThat(application.getApproverComments(),is("Self approval, within green zone"));
    }

    @Test
    public void shouldFindFlyAreaIntersectWithGivenZones() throws Exception {

        //given
        String zones = IOUtils.toString(this.getClass().getResourceAsStream("/geoJsonGreenZones.json"), "UTF-8");
        LatLong one = new LatLong(11.630715737981486, 68.88427734374999);
        LatLong two = new LatLong(7.18810087117902, 68.70849609375);
        LatLong three = new LatLong(11.695272733029402, 77.89306640625);
        LatLong four = new LatLong(14.817370620155254, 77.58544921874999);
        LatLong five = new LatLong(11.630715737981486, 68.88427734374999);

        //intersects with given zone
        assertThat(service.isFlyAreaIntersects(zones, asList(one, two, three, four, five)), is(true));
    }

    @Test
    public void shouldFindFlyAreaIntersectWithGivenZonesWhenWithinZone() throws Exception {

        //given
        String zones = IOUtils.toString(this.getClass().getResourceAsStream("/geoJsonGreenZones.json"), "UTF-8");
        LatLong one = new LatLong(12.232654837013484, 75.87158203125);
        LatLong two = new LatLong(11.802834233547687, 76.168212890625);
        LatLong three = new LatLong(11.77057019562524, 76.761474609375);
        LatLong four = new LatLong(12.302435369557129, 77.003173828125);
        LatLong five = new LatLong(12.538477567560662, 76.4044189453125);
        LatLong six = new LatLong(12.232654837013484, 75.87158203125);

        //within given zone
        assertThat(service.isFlyAreaIntersects(zones, asList(one, two, three, four, five, six)), is(true));
    }

    private FlyDronePermissionApplication application(LocalDateTime dateTime) {
        FlyDronePermissionApplication application = new FlyDronePermissionApplication();
        application.setLastModifiedDate(Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant()));
        return application;
    }
}