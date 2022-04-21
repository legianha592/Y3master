package com.y3technologies.masters.repository;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.datatables.repository.DataTablesRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import com.querydsl.core.types.Predicate;
import com.y3technologies.masters.model.BaseEntity;

@NoRepositoryBean
public interface BaseRepository<T extends BaseEntity> extends JpaRepository<T, Long>, JpaSpecificationExecutor<T>,
		DataTablesRepository<T, Long>, QuerydslPredicateExecutor<T> {

	T getById(Long id);

	List<T> findAll(Predicate predicate);

	List<T> findAll(Predicate predicate, Sort sort);

}