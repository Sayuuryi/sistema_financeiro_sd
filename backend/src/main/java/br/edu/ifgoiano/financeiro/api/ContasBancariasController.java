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
import br.edu.ifgoiano.financeiro.api.Dtos.ContaBancariaRequest;
import br.edu.ifgoiano.financeiro.api.Dtos.ContaBancariaResponse;
import br.edu.ifgoiano.financeiro.servico.CadastrosService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/contas-bancarias")
@Tag(name = "Cadastros")
public class ContasBancariasController {
   private final CadastrosService service;

   public ContasBancariasController(CadastrosService service) {
      this.service = service;
   }

   @ApiResponse(responseCode = "200", description = "Operação realizada")
   @GetMapping
   public List<ContaBancariaResponse> listar() {
      return service.listarContasBancarias();
   }

   @ApiResponse(responseCode = "201", description = "Registro criado")
   @PostMapping
   public ResponseEntity<ContaBancariaResponse> criar(@Valid @RequestBody ContaBancariaRequest r) {
      var c = service.salvarContaBancaria(null, r);
      return ResponseEntity.created(URI.create("/api/contas-bancarias/" + c.id())).body(c);
   }

   @ApiResponse(responseCode = "200", description = "Operação realizada")
   @PutMapping("/{id}")
   public ContaBancariaResponse editar(@PathVariable Long id, @Valid @RequestBody ContaBancariaRequest r) {
      return service.salvarContaBancaria(id, r);
   }

   @ApiResponse(responseCode = "204", description = "Registro excluído")
   @DeleteMapping("/{id}")
   public ResponseEntity<Void> excluir(@PathVariable Long id) {
      service.excluirContaBancaria(id);
      return ResponseEntity.noContent().build();
   }
}
