package com.example.asmproject.service;

import com.example.asmproject.model.Address;
import com.example.asmproject.model.User;
import com.example.asmproject.repository.AddressRepository;
import com.example.asmproject.repository.OrderRepository;
import com.example.asmproject.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AddressService {
    
    @Autowired
    private AddressRepository addressRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    private static final int MAX_ADDRESSES = 4;
    
    public List<Address> getUserAddresses(Long userId) {
        return addressRepository.findByUserIdOrderByDefaultDesc(userId);
    }
    
    public Address addAddress(Long userId, String fullName, String phone,
                             String province, String district, String ward, String street) {
        long addressCount = addressRepository.countByUserId(userId);
        if (addressCount >= MAX_ADDRESSES) {
            throw new RuntimeException("Bạn chỉ có thể thêm tối đa " + MAX_ADDRESSES + " địa chỉ");
        }
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
        
        Address address = new Address();
        address.setUser(user);
        address.setFullName(fullName);
        address.setPhone(phone);
        address.setProvince(province);
        address.setDistrict(district);
        address.setWard(ward);
        address.setStreet(street);
        
        // If this is the first address, set it as default
        if (addressCount == 0) {
            address.setIsDefault(true);
        } else {
            address.setIsDefault(false);
        }
        
        return addressRepository.save(address);
    }
    
    public Address updateAddress(Long addressId, String fullName, String phone,
                                String province, String district, String ward, String street) {
        Address address = addressRepository.findById(addressId)
            .orElseThrow(() -> new RuntimeException("Địa chỉ không tồn tại"));
        
        address.setFullName(fullName);
        address.setPhone(phone);
        address.setProvince(province);
        address.setDistrict(district);
        address.setWard(ward);
        address.setStreet(street);
        
        return addressRepository.save(address);
    }
    
    public void setDefaultAddress(Long userId, Long addressId) {
        // Remove default from all addresses
        List<Address> addresses = addressRepository.findByUserId(userId);
        addresses.forEach(addr -> addr.setIsDefault(false));
        addressRepository.saveAll(addresses);
        
        // Set new default
        Address address = addressRepository.findById(addressId)
            .orElseThrow(() -> new RuntimeException("Địa chỉ không tồn tại"));
        
        if (!address.getUser().getId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền thay đổi địa chỉ này");
        }
        
        address.setIsDefault(true);
        addressRepository.save(address);
    }
    
    public void deleteAddress(Long userId, Long addressId) {
        Address address = addressRepository.findById(addressId)
            .orElseThrow(() -> new RuntimeException("Địa chỉ không tồn tại"));
        
        if (!address.getUser().getId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền xóa địa chỉ này");
        }
        
        // Check if address is being used in any order (due to ON DELETE NO ACTION constraint)
        long orderCount = orderRepository.countByAddressId(addressId);
        if (orderCount > 0) {
            throw new RuntimeException("Không thể xóa địa chỉ này vì đang được sử dụng trong " + orderCount + " đơn hàng. Vui lòng xóa các đơn hàng trước.");
        }
        
        addressRepository.delete(address);
        
        // If deleted address was default, set another one as default
        if (address.getIsDefault()) {
            Optional<Address> firstAddress = addressRepository.findByUserIdOrderByDefaultDesc(userId)
                .stream()
                .findFirst();
            firstAddress.ifPresent(addr -> {
                addr.setIsDefault(true);
                addressRepository.save(addr);
            });
        }
    }
    
    public Optional<Address> getDefaultAddress(Long userId) {
        return addressRepository.findByUserIdAndIsDefaultTrue(userId);
    }
}

