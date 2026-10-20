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
import br.edu.ifgoiano.financeiro.api.Dtos.CategoriaRequest;
import br.edu.ifgoiano.financeiro.api.Dtos.CategoriaResponse;
import br.edu.ifgoiano.financeiro.servico.CadastrosService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/categorias")
@Tag(name = "Cadastros")
public class CategoriasController {
   private final CadastrosService service;

   public CategoriasController(CadastrosService service) {
      this.service = service;
   }

   @ApiResponse(responseCode = "200", description = "Operação realizada")
   @GetMapping
   public List<CategoriaResponse> listar() {
      return service.listarCategorias();
   }

   @ApiResponse(responseCode = "201", description = "Registro criado")
   @PostMapping
   public ResponseEntity<CategoriaResponse> criar(@Valid @RequestBody CategoriaRequest r) {
      var c = service.salvarCategoria(null, r);
      return ResponseEntity.created(URI.create("/api/categorias/" + c.id())).body(c);
   }

   @ApiResponse(responseCode = "200", description = "Operação realizada")
   @PutMapping("/{id}")
   public CategoriaResponse editar(@PathVariable Long id, @Valid @RequestBody CategoriaRequest r) {
      return service.salvarCategoria(id, r);
   }

   @ApiResponse(responseCode = "204", description = "Registro excluído")
   @DeleteMapping("/{id}")
   public ResponseEntity<Void> excluir(@PathVariable Long id) {
      service.excluirCategoria(id);
      return ResponseEntity.noContent().build();
   }
}
