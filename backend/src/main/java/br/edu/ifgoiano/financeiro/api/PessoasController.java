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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.edu.ifgoiano.financeiro.api.Dtos.*;
import br.edu.ifgoiano.financeiro.api.Dtos.PessoaRequest;
import br.edu.ifgoiano.financeiro.api.Dtos.PessoaResponse;
import br.edu.ifgoiano.financeiro.dominio.TipoPessoa;
import br.edu.ifgoiano.financeiro.servico.CadastrosService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/pessoas")
@Tag(name = "Cadastros")
public class PessoasController {
   private final CadastrosService service;

   public PessoasController(CadastrosService service) {
      this.service = service;
   }

   @ApiResponse(responseCode = "200", description = "Operação realizada")
   @GetMapping
   public List<PessoaResponse> listar(@RequestParam(required = false) String busca,
         @RequestParam(required = false) TipoPessoa tipo, @RequestParam(required = false) Boolean ativos) {
      return service.listarPessoas(busca, tipo, ativos);
   }

   @ApiResponse(responseCode = "201", description = "Registro criado")
   @PostMapping
   public ResponseEntity<PessoaResponse> criar(@Valid @RequestBody PessoaRequest r) {
      var c = service.salvarPessoa(null, r);
      return ResponseEntity.created(URI.create("/api/pessoas/" + c.id())).body(c);
   }

   @ApiResponse(responseCode = "200", description = "Operação realizada")
   @PutMapping("/{id}")
   public PessoaResponse editar(@PathVariable Long id, @Valid @RequestBody PessoaRequest r) {
      return service.salvarPessoa(id, r);
   }

   @ApiResponse(responseCode = "204", description = "Registro excluído")
   @DeleteMapping("/{id}")
   public ResponseEntity<Void> excluir(@PathVariable Long id) {
      service.excluirPessoa(id);
      return ResponseEntity.noContent().build();
   }
}
