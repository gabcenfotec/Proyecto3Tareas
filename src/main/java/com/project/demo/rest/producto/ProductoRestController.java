package com.project.demo.rest.producto;

import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.http.Meta;
import com.project.demo.logic.entity.producto.Producto;
import com.project.demo.logic.entity.producto.ProductoRepository;
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
@RequestMapping("/productos")
public class ProductoRestController {
    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Producto> productosPage = productoRepository.findAll(pageable);
        Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
        meta.setTotalPages(productosPage.getTotalPages());
        meta.setTotalElements(productosPage.getTotalElements());
        meta.setPageNumber(productosPage.getNumber() + 1);
        meta.setPageSize(productosPage.getSize());

        return new GlobalResponseHandler().handleResponse("Productos obtenidos correctamente",
                productosPage.getContent(), HttpStatus.OK, meta);
    }

    @GetMapping("/categoria/{categoriaId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAllByCategoria(@PathVariable Long categoriaId,
                                               @RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "10") int size,
                                               HttpServletRequest request) {
        Optional<Categoria> foundCategoria = categoriaRepository.findById(categoriaId);
        if (foundCategoria.isPresent()) {

            Pageable pageable = PageRequest.of(page - 1, size);
            Page<Producto> productosPage = productoRepository.findByCategoriaId(categoriaId, pageable);
            Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
            meta.setTotalPages(productosPage.getTotalPages());
            meta.setTotalElements(productosPage.getTotalElements());
            meta.setPageNumber(productosPage.getNumber() + 1);
            meta.setPageSize(productosPage.getSize());

            return new GlobalResponseHandler().handleResponse("Productos de la categoría obtenidos correctamente",
                    productosPage.getContent(), HttpStatus.OK, meta);
        } else {
            return new GlobalResponseHandler().handleResponse("Categoría id " + categoriaId + " no encontrada",
                    HttpStatus.NOT_FOUND, request);
        }
    }

    @PostMapping("/categoria/{categoriaId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN_ROLE')")
    public ResponseEntity<?> addProductoToCategoria(@PathVariable Long categoriaId, @RequestBody Producto producto, HttpServletRequest request) {
        Optional<Categoria> foundCategoria = categoriaRepository.findById(categoriaId);
        if (foundCategoria.isPresent()) {
            producto.setCategoria(foundCategoria.get());
            Producto savedProducto = productoRepository.save(producto);
            return new GlobalResponseHandler().handleResponse("Producto creado correctamente en la categoría",
                    savedProducto, HttpStatus.CREATED, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Categoría id " + categoriaId + " no encontrada",
                    HttpStatus.NOT_FOUND, request);
        }
    }

    @PutMapping("/{productoId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN_ROLE')")
    public ResponseEntity<?> updateProducto(@PathVariable Long productoId, @RequestBody Producto producto, HttpServletRequest request) {
        Optional<Producto> foundProducto = productoRepository.findById(productoId);
        if (foundProducto.isPresent()) {
            producto.setId(foundProducto.get().getId());
            producto.setCategoria(foundProducto.get().getCategoria());
            productoRepository.save(producto);
            return new GlobalResponseHandler().handleResponse("Producto actualizado correctamente",
                    producto, HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Producto id " + productoId + " no encontrado",
                    HttpStatus.NOT_FOUND, request);
        }
    }

    @DeleteMapping("/{productoId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN_ROLE')")
    public ResponseEntity<?> deleteProducto(@PathVariable Long productoId, HttpServletRequest request) {
        Optional<Producto> foundProducto = productoRepository.findById(productoId);
        if (foundProducto.isPresent()) {
            productoRepository.deleteById(productoId);
            return new GlobalResponseHandler().handleResponse("Producto eliminado correctamente",
                    foundProducto.get(), HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Producto id " + productoId + " no encontrado",
                    HttpStatus.NOT_FOUND, request);
        }
    }
}
