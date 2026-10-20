package br.edu.ifgoiano.financeiro.api;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.edu.ifgoiano.financeiro.api.Dtos.*;
import br.edu.ifgoiano.financeiro.api.Dtos.BancoRequest;
import br.edu.ifgoiano.financeiro.api.Dtos.BancoResponse;
import br.edu.ifgoiano.financeiro.servico.CadastrosService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/bancos")
@Tag(name = "Cadastros")
public class BancosController {
   private final CadastrosService service;

   public BancosController(CadastrosService service) {
      this.service = service;
   }

   @ApiResponse(responseCode = "200", description = "Operação realizada")
   @GetMapping
   public List<BancoResponse> listar() {
      return service.listarBancos();
   }

   @ApiResponse(responseCode = "201", description = "Registro criado")
   @PostMapping
   public ResponseEntity<BancoResponse> criar(@Valid @RequestBody BancoRequest r) {
      var c = service.salvarBanco(null, r);
      return ResponseEntity.created(URI.create("/api/bancos/" + c.id())).body(c);
   }

   @ApiResponse(responseCode = "200", description = "Operação realizada")
   @PutMapping("/{id}")
   public BancoResponse editar(@PathVariable Long id, @Valid @RequestBody BancoRequest r) {
      return service.salvarBanco(id, r);
   }

   @ApiResponse(responseCode = "204", description = "Registro excluído")
   @DeleteMapping("/{id}")
   public ResponseEntity<Void> excluir(@PathVariable Long id) {
      service.excluirBanco(id);
      return ResponseEntity.noContent().build();
   }
}
