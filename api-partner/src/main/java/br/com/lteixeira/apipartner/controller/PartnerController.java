package br.com.lteixeira.apipartner.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/parceiros")
public class PartnerController {

    @GetMapping("/magalu")
    public ResponseEntity<String> magaluInfo(@RequestHeader("client_id") final String clientId, final HttpServletRequest httpServletRequest) {

        final Optional<String> header = Optional.ofNullable(httpServletRequest.getHeader("gw-correlation"));

        if (header.isPresent()) {

            log.info("Request realizada no recurso /magalu com a clientId: {}", clientId);

            return ResponseEntity.ok("Magazine Luiza");
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Acesso negado.");

    }

    @GetMapping("/gpa")
    public ResponseEntity<String> gpaInfo(@RequestHeader("client_id") final String clientId, final HttpServletRequest httpServletRequest) {

        final Optional<String> header = Optional.ofNullable(httpServletRequest.getHeader("gw-correlation"));

        if (header.isPresent()) {

            log.info("Request realizada no recurso /gpa com o clientId: {}", clientId);

            return ResponseEntity.ok("Grupo Pão de Açucar");
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Acesso negado.");

    }
}
