package com.example.mdbspringboot;

//import com.example.mdbspringboot.model.Patient;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.RestfulServer;
import com.example.mdbspringboot.model.CustomPatient;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r5.model.Patient;
//import ca.uhn.fhir.rest.annotation.IdParam;
//import ca.uhn.fhir.rest.annotation.Read;
//import ca.uhn.fhir.rest.annotation.RequiredParam;
//import ca.uhn.fhir.rest.annotation.Search;
//import ca.uhn.fhir.rest.param.StringParam;
//import ca.uhn.fhir.rest.server.IResourceProvider;
//import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
//import org.hl7.fhir.dstu2.model.IdType;
//import org.hl7.fhir.instance.model.api.IBaseResource;
//import org.hl7.fhir.r5.model.Patient;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletException;

@RestController
@RequestMapping("/mypatient")
//@RequiredArgsConstructor
public class CustomController extends RestfulServer {
   // private final PatientService patientService;
    FhirContext ctx = FhirContext.forR4();

    @Override
    protected void initialize()throws ServletException {

        this.ctx = FhirContext.forR4();
    }

  //  @Autowired
   // public PatientController(PatientService patientService) {
   //     this.patientService = patientService;
   // }

    @PostMapping()
    // @RequestMapping("/mypatient")
    public ResponseEntity addMyPatient(@RequestBody CustomPatient mypatient)
    {
        // patientService.addPatient(mypatient);
        FhirApiReq far = new FhirApiReq();
        IParser jsonparser = this.ctx.newJsonParser();

        String serOrg = jsonparser.encodeResourceToString(mypatient);
        far.ApiPost(serOrg, "Patient");

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


  //  @GetMapping("/{Pid}")
  //  public ResponseEntity getPatient(@PathVariable String Pid) {
     //   return ResponseEntity.ok(patientService.getPatient(Pid));
   // }
    @GetMapping()
    public ResponseEntity testPatient() {
        return ResponseEntity.ok("Test ok reached Patient controller");
    }
}
//checked upto update 22-09-21


