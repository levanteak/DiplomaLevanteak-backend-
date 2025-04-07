package com.diplom.levanteak.repository;

import com.diplom.levanteak.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepo extends JpaRepository<Address, Long> {
}
