package com.example.asmproject.service;

import com.example.asmproject.dto.ProductDetailDTO;
import com.example.asmproject.dto.ProductRequest;
import com.example.asmproject.dto.ProductResponse;
import com.example.asmproject.model.Product;
import com.example.asmproject.repository.BrandRepository;
import com.example.asmproject.repository.CategoryRepository;
import com.example.asmproject.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service xử lý các logic nghiệp vụ liên quan đến sản phẩm
 * Bao gồm: tìm kiếm sản phẩm, lấy danh sách sản phẩm, lấy chi tiết sản phẩm
 * 
 * @author VinFast Development Team
 * @version 1.0
 */
@Service
@Transactional
public class ProductService {
    
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private CategoryRepository categoryRepository;
    
    /**
     * Tìm kiếm sản phẩm với nhiều tiêu chí
     * 
     * Logic tìm kiếm:
     * - keyword: Tìm trong tên sản phẩm (name) hoặc mô tả (description)
     *            Sử dụng LIKE với wildcard %keyword% (không phân biệt hoa thường)
     * - brandId: Filter theo thương hiệu
     * - categoryId: Filter theo danh mục
     * - status: Filter theo trạng thái (ACTIVE, INACTIVE, OUT_OF_STOCK)
     * 
     * Kết quả được phân trang
     * 
     * @param keyword Từ khóa tìm kiếm (tìm trong name hoặc description)
     * @param brandId ID của thương hiệu (optional, null = tất cả thương hiệu)
     * @param categoryId ID của danh mục (optional, null = tất cả danh mục)
     * @param status Trạng thái sản phẩm (optional, null = tất cả trạng thái)
     * @param pageable Thông tin phân trang (page, size, sort)
     * @return Page<Product> - Danh sách sản phẩm đã được phân trang
     */
    public Page<Product> searchProducts(String keyword, Long brandId, Long categoryId, 
                                       Product.ProductStatus status, Pageable pageable) {
        // Gọi repository để tìm kiếm sản phẩm
        // Repository sẽ thực hiện query SQL với các điều kiện trên
        return productRepository.searchProducts(keyword, brandId, categoryId, status, pageable);
    }
    
    /**
     * Lấy tất cả sản phẩm đang hoạt động (status = ACTIVE)
     * Không phân trang, trả về toàn bộ danh sách
     * 
     * @return Danh sách tất cả sản phẩm đang hoạt động
     */
    public List<Product> getAllActiveProducts() {
        // Query sản phẩm có status = ACTIVE
        return productRepository.findByStatus(Product.ProductStatus.ACTIVE);
    }
    
    /**
     * Lấy danh sách sản phẩm đang hoạt động với phân trang
     * 
     * @param pageable Thông tin phân trang (page, size, sort)
     * @return Page<Product> - Danh sách sản phẩm đang hoạt động đã được phân trang
     */
    public Page<Product> getActiveProducts(Pageable pageable) {
        // Query sản phẩm có status = ACTIVE với phân trang
        return productRepository.findByStatus(Product.ProductStatus.ACTIVE, pageable);
    }
    
    /**
     * Lấy chi tiết sản phẩm theo ID
     * 
     * @param id ID của sản phẩm
     * @return Optional<Product> - có thể empty nếu không tìm thấy
     */
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }
    
    /**
     * Lấy chi tiết sản phẩm theo slug (URL-friendly name)
     * Slug được dùng trong URL thay vì ID để SEO tốt hơn
     * 
     * Ví dụ: slug "vinfast-klara-a1" thay vì ID 123
     * 
     * @param slug Slug của sản phẩm (URL-friendly name)
     * @return Optional<Product> - có thể empty nếu không tìm thấy
     */
    public Optional<Product> getProductBySlug(String slug) {
        return productRepository.findBySlug(slug);
    }
    
    /**
     * Lấy chi tiết sản phẩm theo slug với JOIN FETCH để tránh lazy loading
     * Trả về ProductDetailDTO để tránh vấn đề lazy loading exception
     * 
     * @param slug Slug của sản phẩm
     * @return Optional<ProductDetailDTO> - có thể empty nếu không tìm thấy
     */
    @Transactional(readOnly = true)
    public Optional<ProductDetailDTO> getProductDetailBySlug(String slug) {
        Optional<Product> productOpt = productRepository.findBySlugWithBrandAndCategory(slug);
        return productOpt.map(ProductDetailDTO::new);
    }
    
    /**
     * Lấy sản phẩm theo slug với tất cả thông tin liên quan (brand, category, productColors, color)
     * Sử dụng JOIN FETCH để tránh lazy loading exception
     * 
     * @param slug Slug của sản phẩm
     * @return Optional<Product> - có thể empty nếu không tìm thấy
     */
    @Transactional(readOnly = true)
    public Optional<Product> getProductBySlugWithDetails(String slug) {
        Optional<Product> productOpt = productRepository.findBySlugWithBrandAndCategory(slug);
        // Initialize lazy collections
        productOpt.ifPresent(product -> {
            if (product.getProductImages() != null) {
                product.getProductImages().size(); // Trigger fetch
            }
        });
        return productOpt;
    }
    
    /**
     * Tạo sản phẩm mới từ ProductRequest
     */
    public ProductResponse createProduct(ProductRequest request) {
        Product product = new Product();
        mapRequestToProduct(request, product);
        
        // Generate slug if not provided
        if (product.getSlug() == null || product.getSlug().isEmpty()) {
            product.setSlug(generateSlug(product.getName()));
        }
        
        // Check unique slug
        if (productRepository.findBySlug(product.getSlug()).isPresent()) {
            throw new RuntimeException("Slug đã tồn tại: " + product.getSlug());
        }
        
        Product saved = productRepository.save(product);
        return toProductResponse(saved);
    }

    /**
     * Cập nhật sản phẩm từ ProductRequest
     */
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));
        
        mapRequestToProduct(request, product);
        
        // Check unique slug if changed
        if (request.getSlug() != null && !request.getSlug().equals(product.getSlug())) {
            if (productRepository.findBySlug(request.getSlug()).isPresent()) {
                throw new RuntimeException("Slug đã tồn tại: " + request.getSlug());
            }
        }
        
        Product saved = productRepository.save(product);
        return toProductResponse(saved);
    }

    /**
     * Helper method: Map ProductRequest to Product
     */
    private void mapRequestToProduct(ProductRequest request, Product product) {
        product.setName(request.getName());
        if (request.getSlug() != null && !request.getSlug().isEmpty()) {
            product.setSlug(request.getSlug());
        }
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setDiscountPrice(request.getDiscountPrice());
        product.setQuantity(request.getQuantity());
        product.setImage(request.getImage());
        product.setImages(request.getImages());
        product.setSpecifications(request.getSpecifications());
        product.setStatus(request.getStatus());
        
        if (request.getBrandId() != null) {
            product.setBrand(brandRepository.findById(request.getBrandId()).orElse(null));
        }
        
        if (request.getCategoryId() != null) {
            product.setCategory(categoryRepository.findById(request.getCategoryId()).orElse(null));
        }
    }

    /**
     * Helper method: Map Product to ProductResponse
     */
    public ProductResponse toProductResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setSlug(product.getSlug());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
        response.setDiscountPrice(product.getDiscountPrice());
        response.setQuantity(product.getQuantity());
        response.setImage(product.getImage());
        response.setImages(product.getImages());
        response.setSpecifications(product.getSpecifications());
        response.setStatus(product.getStatus());
        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());
        
        if (product.getBrand() != null) {
            response.setBrandId(product.getBrand().getId());
            response.setBrandName(product.getBrand().getName());
        }
        
        if (product.getCategory() != null) {
            response.setCategoryId(product.getCategory().getId());
            response.setCategoryName(product.getCategory().getName());
        }
        
        return response;
    }

    /**
     * Helper method: Generate slug from string
     */
    private String generateSlug(String input) {
        if (input == null) return "";
        return input.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-");
    }

    /**
     * Lưu sản phẩm (thêm mới hoặc cập nhật)
     * 
     * @param product Product object cần lưu
     * @return Product object đã được lưu vào database
     */
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }
    
    /**
     * Xóa sản phẩm theo ID
     * 
     * @param id ID của sản phẩm cần xóa
     */
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
    
    /**
     * Lấy danh sách sản phẩm theo thương hiệu
     * 
     * @param brandId ID của thương hiệu
     * @return Danh sách sản phẩm thuộc thương hiệu đó
     */
    public List<Product> getProductsByBrand(Long brandId) {
        return productRepository.findByBrandId(brandId);
    }
    
    /**
     * Lấy danh sách sản phẩm theo danh mục
     * 
     * @param categoryId ID của danh mục
     * @return Danh sách sản phẩm thuộc danh mục đó
     */
    public List<Product> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }
    
    /**
     * Đếm tổng số sản phẩm đang hoạt động (status = ACTIVE)
     * 
     * @return Tổng số sản phẩm đang hoạt động
     */
    public long getTotalActiveProducts() {
        return productRepository.countActiveProducts();
    }
    
    /**
     * Đếm tổng số sản phẩm hết hàng (quantity <= 0)
     * 
     * @return Tổng số sản phẩm hết hàng
     */
    public long getTotalOutOfStockProducts() {
        return productRepository.countOutOfStockProducts();
    }
}

