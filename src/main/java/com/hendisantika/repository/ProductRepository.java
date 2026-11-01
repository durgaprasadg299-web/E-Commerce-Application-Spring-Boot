package com.hendisantika.repository;

import com.hendisantika.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * Project : spring-boot-ecommerce
 * User: powercommerce
 * Email: hendisantika@gmail.com
 * Telegram : @hendisantika34
 * Date: 7/30/22
 * Time: 20:37
 * To change this template use File | Settings | File Templates.
 */
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByNameContainingIgnoreCase(String name);

    @Query("""
            select distinct p from Product p
            left join p.categories c
            where (:search is null or lower(p.name) like lower(concat('%', :search, '%')))
              and (:brand is null or lower(p.brand) = lower(:brand))
              and (:categoryId is null or c.id = :categoryId)
              and (:maxPrice is null or p.price <= :maxPrice)
            order by p.name
            """)
    List<Product> findCatalogProducts(@Param("search") String search,
                                      @Param("brand") String brand,
                                      @Param("categoryId") Long categoryId,
                                      @Param("maxPrice") Double maxPrice);

    @Query("select distinct p.brand from Product p where p.brand is not null and p.brand <> '' order by p.brand")
    List<String> findAllBrandsDistincts();
}
