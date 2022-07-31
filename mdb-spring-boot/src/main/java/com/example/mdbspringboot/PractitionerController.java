package com.example.mdbspringboot;


import com.example.mdbspringboot.model.Patient;
import com.example.mdbspringboot.model.Practitioner;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/practitioner")

public class PractitionerController {
    private final PractitionerService practitionerService;

    @Autowired
    public PractitionerController(PractitionerService practitionerService) {

        this.practitionerService = practitionerService;
    }

    @PostMapping
    public ResponseEntity addPractitioner(@RequestBody Practitioner practitioner)
    {
        practitionerService.addPractitioner(practitioner);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    @PutMapping
    public ResponseEntity updatePractitioner(@RequestBody Practitioner practitioner)
    {
        practitionerService.updatePractitioner(practitioner);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    @GetMapping("/{Pracid}")
    public ResponseEntity getPractitioner(@PathVariable String Pracid) {
        return ResponseEntity.ok(practitionerService.getPractitioner(Pracid));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity deletePractitioner(@PathVariable String id) {
        practitionerService.deletePractitioner(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping()
    public ResponseEntity testPractitioner() {
        return ResponseEntity.ok("Test ok reached Practitioner controller");
    }

}
