package com.cyFramework.core;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/messages")
public class MessageController {

    @PostMapping
    public ResponseEntity<Void> recevoirMessage(@RequestBody Message message) {
        Acteur acteur = ActorRegistry.trouver(message.getDestinataire());
        if (acteur != null) {
            acteur.recevoirMessage(message);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
