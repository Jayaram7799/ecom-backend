package in.btm.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

	private String name;

	private String email;

	private String phone;


	private boolean enabled = true;

	private List<AddressDto> addresses = new ArrayList<>();
}
