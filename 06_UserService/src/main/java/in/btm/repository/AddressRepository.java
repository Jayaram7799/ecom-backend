package in.btm.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import in.btm.entity.Address;

public interface AddressRepository extends JpaRepository<Address, Integer> {
	List<Address> findByUserEmail(String email);
}
