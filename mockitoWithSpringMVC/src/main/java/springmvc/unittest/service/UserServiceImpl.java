package springmvc.unittest.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Service;

import springmvc.unittest.model.User;

@Service
public class UserServiceImpl implements UserService {

	private static final AtomicInteger counter = new AtomicInteger();
	private static List<User> users = new ArrayList<User>(
			Arrays.asList(
						new User(counter.incrementAndGet(), "VanVTT"), 
						new User(counter.incrementAndGet(), "TrungHN"),
						new User(counter.incrementAndGet(), "HuyHM"), 
						new User(counter.incrementAndGet(), "ThaoDTD")));

	@Override
	public List<User> getAll() {
		// TODO Auto-generated method stub
		return users;
	}

	@Override
	public User findById(int id) {
		for (User user : users) {
			if (user.getId() == id)
				return user;
		}
		return null;
	}

	@Override
	public User findByName(String name) {
		for (User user : users) {
			if (name.equals(user.getName()))
				return user;
		}
		return null;
	}

	@Override
	public void create(User user) {
		user.setId(counter.incrementAndGet());
		users.add(user);
	}

	@Override
	public void update(User user) {
		int index = users.indexOf(findById(user.getId()));
        users.set(index, user);
	}

	@Override
	public void delete(int id) {
		User user = findById(id);
        users.remove(user);
	}

	@Override
	public boolean exists(User user) {
		return findByName(user.getName()) != null;
	}

}
