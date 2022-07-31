package com.example.mdbspringboot;

import com.example.mdbspringboot.model.Encounter;
import com.example.mdbspringboot.model.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/Encounter")
public class EncounterController {
    private final EncounterService encounterService;

    @Autowired
    public EncounterController(EncounterService encounterService) {

        this.encounterService = encounterService;
    }

    @PostMapping
    public ResponseEntity addEncounter(@RequestBody Encounter encounter)
    {
        encounterService.addEncounter(encounter);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    @PutMapping
    public ResponseEntity updateEncounter(@RequestBody Encounter encounter)
    {
        encounterService.updateEncounter(encounter);
        return ResponseEntity.status(HttpStatus.CREATED).build();

    }
    @DeleteMapping("/{id}")
    public ResponseEntity deleteEncounter(@PathVariable String id) {
        encounterService.deleteEncounter(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping()
    public ResponseEntity testEncounter() {
        return ResponseEntity.ok("Test ok reached Encounter controller");
    }

    @GetMapping("/{Eid}")
    public ResponseEntity getEncounter(@PathVariable String Eid) {
        return ResponseEntity.ok(encounterService.getEncounter(Eid));
    }


}
