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

/**
 * Service xử lý các logic nghiệp vụ liên quan đến quản lý địa chỉ
 * Bao gồm: thêm, sửa, xóa địa chỉ, đặt địa chỉ mặc định
 * 
 * Giới hạn: Mỗi user chỉ có thể thêm tối đa 4 địa chỉ
 * 
 * @author VinFast Development Team
 * @version 1.0
 */
@Service
@Transactional
public class AddressService {
    
    @Autowired
    private AddressRepository addressRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    /**
     * Số lượng địa chỉ tối đa mà mỗi user có thể có
     * Giới hạn này để đảm bảo UI không quá phức tạp và dễ quản lý
     */
    private static final int MAX_ADDRESSES = 4;
    
    /**
     * Lấy danh sách địa chỉ của user
     * Sắp xếp theo địa chỉ mặc định trước (default = true), sau đó mới đến các địa chỉ khác
     * 
     * @param userId ID của người dùng
     * @return Danh sách địa chỉ của user (địa chỉ mặc định ở đầu)
     */
    public List<Address> getUserAddresses(Long userId) {
        // Query từ database và sắp xếp theo địa chỉ mặc định trước
        return addressRepository.findByUserIdOrderByDefaultDesc(userId);
    }
    
    /**
     * Thêm địa chỉ mới cho user
     * 
     * Logic:
     * - Kiểm tra số lượng địa chỉ hiện tại (tối đa 4)
     * - Nếu đây là địa chỉ đầu tiên thì tự động đặt làm mặc định
     * - Nếu không phải thì đặt isDefault = false
     * 
     * @param userId ID của người dùng
     * @param fullName Họ tên người nhận
     * @param phone Số điện thoại
     * @param province Tỉnh/Thành phố
     * @param district Quận/Huyện
     * @param ward Phường/Xã
     * @param street Đường/Số nhà
     * @return Address object đã được lưu vào database
     * @throws RuntimeException nếu đã có 4 địa chỉ hoặc user không tồn tại
     */
    public Address addAddress(Long userId, String fullName, String phone,
                             String province, String district, String ward, String street) {
        // Kiểm tra số lượng địa chỉ hiện tại
        // Mỗi user chỉ có thể có tối đa MAX_ADDRESSES địa chỉ
        long addressCount = addressRepository.countByUserId(userId);
        if (addressCount >= MAX_ADDRESSES) {
            throw new RuntimeException("Bạn chỉ có thể thêm tối đa " + MAX_ADDRESSES + " địa chỉ. Vui lòng xóa một địa chỉ hiện có để thêm địa chỉ mới.");
        }
        
        // Kiểm tra user có tồn tại không
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
        
        // Tạo địa chỉ mới
        Address address = new Address();
        address.setUser(user);
        address.setFullName(fullName);
        address.setPhone(phone);
        address.setProvince(province);
        address.setDistrict(district);
        address.setWard(ward);
        address.setStreet(street);
        
        // Nếu đây là địa chỉ đầu tiên thì tự động đặt làm mặc định
        // Nếu không phải thì đặt isDefault = false
        if (addressCount == 0) {
            address.setIsDefault(true);
        } else {
            address.setIsDefault(false);
        }
        
        // Lưu vào database
        return addressRepository.save(address);
    }
    
    /**
     * Cập nhật thông tin địa chỉ
     * 
     * @param addressId ID của địa chỉ cần cập nhật
     * @param fullName Họ tên người nhận mới
     * @param phone Số điện thoại mới
     * @param province Tỉnh/Thành phố mới
     * @param district Quận/Huyện mới
     * @param ward Phường/Xã mới
     * @param street Đường/Số nhà mới
     * @return Address object đã được cập nhật
     * @throws RuntimeException nếu địa chỉ không tồn tại
     */
    public Address updateAddress(Long addressId, String fullName, String phone,
                                String province, String district, String ward, String street) {
        // Tìm địa chỉ theo ID
        Address address = addressRepository.findById(addressId)
            .orElseThrow(() -> new RuntimeException("Địa chỉ không tồn tại"));
        
        // Cập nhật thông tin
        address.setFullName(fullName);
        address.setPhone(phone);
        address.setProvince(province);
        address.setDistrict(district);
        address.setWard(ward);
        address.setStreet(street);
        
        // Lưu vào database
        return addressRepository.save(address);
    }
    
    /**
     * Đặt địa chỉ làm mặc định
     * Chỉ có 1 địa chỉ mặc định tại một thời điểm
     * Khi đặt địa chỉ mới làm mặc định thì địa chỉ cũ sẽ tự động bỏ mặc định
     * 
     * @param userId ID của người dùng (để xác thực quyền)
     * @param addressId ID của địa chỉ muốn đặt làm mặc định
     * @throws RuntimeException nếu địa chỉ không tồn tại hoặc không thuộc user này
     */
    public void setDefaultAddress(Long userId, Long addressId) {
        // Bước 1: Bỏ mặc định tất cả địa chỉ hiện tại của user
        // Đảm bảo chỉ có 1 địa chỉ mặc định tại một thời điểm
        List<Address> addresses = addressRepository.findByUserId(userId);
        addresses.forEach(addr -> addr.setIsDefault(false));
        addressRepository.saveAll(addresses);
        
        // Bước 2: Tìm địa chỉ muốn đặt làm mặc định
        Address address = addressRepository.findById(addressId)
            .orElseThrow(() -> new RuntimeException("Địa chỉ không tồn tại"));
        
        // Kiểm tra quyền: địa chỉ phải thuộc về user này
        if (!address.getUser().getId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền thay đổi địa chỉ này");
        }
        
        // Đặt địa chỉ này làm mặc định
        address.setIsDefault(true);
        addressRepository.save(address);
    }
    
    /**
     * Xóa địa chỉ
     * 
     * Logic:
     * - Kiểm tra địa chỉ có đang được sử dụng trong đơn hàng không
     *   (do constraint ON DELETE NO ACTION, không thể xóa nếu đang dùng)
     * - Nếu có thì không cho xóa, throw exception
     * - Nếu không thì xóa
     * - Nếu xóa địa chỉ mặc định thì tự động đặt địa chỉ khác làm mặc định (nếu có)
     * 
     * @param userId ID của người dùng (để xác thực quyền)
     * @param addressId ID của địa chỉ cần xóa
     * @throws RuntimeException nếu địa chỉ không tồn tại, không thuộc user này, hoặc đang được sử dụng
     */
    public void deleteAddress(Long userId, Long addressId) {
        // Tìm địa chỉ theo ID
        Address address = addressRepository.findById(addressId)
            .orElseThrow(() -> new RuntimeException("Địa chỉ không tồn tại"));
        
        // Kiểm tra quyền: địa chỉ phải thuộc về user này
        if (!address.getUser().getId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền xóa địa chỉ này");
        }
        
        // Kiểm tra địa chỉ có đang được sử dụng trong đơn hàng không
        // Do constraint ON DELETE NO ACTION, không thể xóa nếu đang được dùng
        long orderCount = orderRepository.countByAddressId(addressId);
        if (orderCount > 0) {
            throw new RuntimeException("Không thể xóa địa chỉ này vì đang được sử dụng trong " + orderCount + " đơn hàng. Vui lòng xóa các đơn hàng trước.");
        }
        
        // Lưu lại thông tin xem địa chỉ này có phải là mặc định không
        boolean wasDefault = address.getIsDefault();
        
        // Xóa địa chỉ
        addressRepository.delete(address);
        
        // Nếu xóa địa chỉ mặc định thì tự động đặt địa chỉ khác làm mặc định
        // Lấy địa chỉ đầu tiên (nếu còn) và đặt làm mặc định
        if (wasDefault) {
            Optional<Address> firstAddress = addressRepository.findByUserIdOrderByDefaultDesc(userId)
                .stream()
                .findFirst();
            firstAddress.ifPresent(addr -> {
                addr.setIsDefault(true);
                addressRepository.save(addr);
            });
        }
    }
    
    /**
     * Lấy địa chỉ mặc định của user
     * 
     * @param userId ID của người dùng
     * @return Optional<Address> - có thể empty nếu user chưa có địa chỉ mặc định
     */
    public Optional<Address> getDefaultAddress(Long userId) {
        // Query địa chỉ có isDefault = true của user
        return addressRepository.findByUserIdAndIsDefaultTrue(userId);
    }
}

