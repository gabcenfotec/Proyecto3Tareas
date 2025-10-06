package com.project.demo.rest.categoria;

import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.http.Meta;
import com.project.demo.logic.entity.Categoria.Categoria;
import com.project.demo.logic.entity.Categoria.CategoriaRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/categorias")
public class CategoriaRestController {
    @Autowired
    private CategoriaRepository CategoriaRepository;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        Pageable pageable = PageRequest.of(page-1, size);
        Page<Categoria> categoriasPage = CategoriaRepository.findAll(pageable);
        Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
        meta.setTotalPages(categoriasPage.getTotalPages());
        meta.setTotalElements(categoriasPage.getTotalElements());
        meta.setPageNumber(categoriasPage.getNumber() + 1);
        meta.setPageSize(categoriasPage.getSize());

        return new GlobalResponseHandler().handleResponse("Categorías obtenidas correctamente",
                categoriasPage.getContent(), HttpStatus.OK, meta);
    }

    @GetMapping("/{categoriaId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getById(@PathVariable Long categoriaId, HttpServletRequest request) {
        Optional<Categoria> foundCategoria = CategoriaRepository.findById(categoriaId);
        if(foundCategoria.isPresent()) {
            return new GlobalResponseHandler().handleResponse("Categoría encontrada correctamente",
                    foundCategoria.get(), HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Categoría id " + categoriaId + " no encontrada",
                    HttpStatus.NOT_FOUND, request);
        }
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN_ROLE')")
    public ResponseEntity<?> addCategoria(@RequestBody Categoria categoria, HttpServletRequest request) {
        Categoria savedCategoria = CategoriaRepository.save(categoria);
        return new GlobalResponseHandler().handleResponse("Categoría creada correctamente",
                savedCategoria, HttpStatus.CREATED, request);
    }

    @PutMapping("/{categoriaId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN_ROLE')")
    public ResponseEntity<?> updateCategoria(@PathVariable Long categoriaId, @RequestBody Categoria categoria, HttpServletRequest request) {
        Optional<Categoria> foundCategoria = CategoriaRepository.findById(categoriaId);
        if(foundCategoria.isPresent()) {
            categoria.setId(foundCategoria.get().getId());
            CategoriaRepository.save(categoria);
            return new GlobalResponseHandler().handleResponse("Categoría actualizada correctamente",
                    categoria, HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Categoría id " + categoriaId + " no encontrada",
                    HttpStatus.NOT_FOUND, request);
        }
    }

    @DeleteMapping("/{categoriaId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN_ROLE')")
    public ResponseEntity<?> deleteCategoria(@PathVariable Long categoriaId, HttpServletRequest request) {
        Optional<Categoria> foundCategoria = CategoriaRepository.findById(categoriaId);
        if(foundCategoria.isPresent()) {
            CategoriaRepository.deleteById(categoriaId);
            return new GlobalResponseHandler().handleResponse("Categoría eliminada correctamente",
                    foundCategoria.get(), HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Categoría id " + categoriaId + " no encontrada",
                    HttpStatus.NOT_FOUND, request);
        }
    }
}
