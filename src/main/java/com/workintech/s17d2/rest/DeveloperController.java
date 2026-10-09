package com.workintech.s17d2.rest;

import com.workintech.s17d2.model.*;
import com.workintech.s17d2.tax.Taxable;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/developers")
public class DeveloperController {

    public Map<Integer, Developer> developers;
    private final Taxable taxable;

    // Dependency Injection: Spring otomatik olarak DeveloperTax bean'ini enjekte eder
    @Autowired
    public DeveloperController(Taxable taxable) {
        this.taxable = taxable;
    }

    // Bean oluştuktan hemen sonra Map ilklendirilir
    @PostConstruct
    public void init() {
        this.developers = new HashMap<>();
    }

    // 1. [GET] /workintech/developers -> Tüm geliştiricileri liste olarak döner
    @GetMapping
    public List<Developer> getAll() {
        return new ArrayList<>(developers.values());
    }

    // 2. [GET] /workintech/developers/{id} -> İlgili id'deki geliştiriciyi döner
    @GetMapping("/{id}")
    public Developer getById(@PathVariable int id) {
        return developers.get(id);
    }

    // 3. [POST] /workintech/developers -> Geliştirici ekler ve vergi düşülmüş maaşı hesaplar
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Developer create(@RequestBody Developer developer) {
        Developer createdDeveloper = null;
        double baseSalary = developer.getSalary();

        if (developer.getExperience() == null) {
            return null;
        }

        switch (developer.getExperience()) {
            case JUNIOR -> {
                double netSalary = baseSalary - (baseSalary * taxable.getSimpleTaxRate() / 100);
                createdDeveloper = new JuniorDeveloper(developer.getId(), developer.getName(), netSalary);
            }
            case MID -> {
                double netSalary = baseSalary - (baseSalary * taxable.getMiddleTaxRate() / 100);
                createdDeveloper = new MidDeveloper(developer.getId(), developer.getName(), netSalary);
            }
            case SENIOR -> {
                double netSalary = baseSalary - (baseSalary * taxable.getUpperTaxRate() / 100);
                createdDeveloper = new SeniorDeveloper(developer.getId(), developer.getName(), netSalary);
            }
        }

        if (createdDeveloper != null) {
            developers.put(createdDeveloper.getId(), createdDeveloper);
        }

        return createdDeveloper;
    }

    // 4. [PUT] /workintech/developers/{id} -> İlgili geliştiriciyi günceller
    @PutMapping("/{id}")
    public Developer update(@PathVariable int id, @RequestBody Developer developer) {
        developer.setId(id);
        developers.put(id, developer);
        return developer;
    }

    // 5. [DELETE] /workintech/developers/{id} -> İlgili geliştiriciyi siler
    @DeleteMapping("/{id}")
    public Developer delete(@PathVariable int id) {
        return developers.remove(id);
    }
}