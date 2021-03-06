package com.dawes.ridersgijon.controller;

import java.util.List;
import java.util.NoSuchElementException;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.dawes.ridersgijon.model.PedidoVO;
import com.dawes.ridersgijon.model.UserRolVO;
import com.dawes.ridersgijon.model.UserVO;
import com.dawes.ridersgijon.service.PedidoService;
import com.dawes.ridersgijon.service.RolService;
import com.dawes.ridersgijon.service.UserRolService;
import com.dawes.ridersgijon.service.UserService;

@Controller
@RequestMapping("/admin")
public class AdminController {	
	@Autowired
	UserService userService;	
	@Autowired
	PedidoService pedidoService;	
	@Autowired
	RolService rolService;	
	@Autowired
	UserRolService userRolService;
	
//ADMINISTRACION*************************************************************************	
	
	/**
	 * Pantalla de Inicio de Administrador tras autenticación
	 * @param model
	 * @return
	 */
	@GetMapping ("")
	public String admin(Model model){
		//Le pasamos el nombre de usuario logueado para mostrarlos en el header de la vista
    	model.addAttribute("nick", userService.findUserLogged().getNick());
    	//Con redirect recargamos la página para mostrar la lista actualizda
		return "redirect:adminList";
	}

	
	/**
	 * Listado de usuarios administradores
	 * @param model
	 * @return
	 */
	@GetMapping ("/adminList")
	public String adminList(Model model){
		//Le pasamos el nombre de usuario logueado para mostrarlos en el header de la vista		
    	model.addAttribute("nick", userService.findUserLogged().getNick());
    	//Le pasamos una colección con todos los usuarios Administradores de la BBDD para mostrarlos en la vista
    	model.addAttribute("listaAdmins", userService.findByUser_type("ADMIN"));
		return "admin/adminAdminsAdminList";
	}
	
	
	/**
	 * Actualizacion de usuario Administrador
	 * @param detalleUser
	 * @param model
	 * @return
	 */
	@PostMapping("/adminList")
	//Recogemos el UserVO con los datos del formulario
	public String adminUpdate(@ModelAttribute ("detalleUser") UserVO detalleUser, Model model){
		//Administradores siempre activos
		detalleUser.setIsActive(true);		
		userService.save(detalleUser);		
		return "redirect:adminList";
	}
	
	/**
	 * Vista de detalle de datos de un Administrador. Se pasa el id por parámetro en la url
	 * @param id_user
	 * @param model
	 * @return
	 */
	@GetMapping ("/adminDetail")
	//Recoge el id pasado como parámetro para recuperar el el UserVO
	public String adminDetail(@RequestParam(name="id_user") int id_user,  Model model){
	//Le mandamos el id del usuario logueado para comparar con el del detalle
	model.addAttribute("adminLogueado", userService.findUserLogged().getId_user());
	//Nick del usuario autenticado
	model.addAttribute("nick", userService.findUserLogged().getNick());
	//Le pasamos el UserVO para mostrar los datos en el formulario de la vista    	
	model.addAttribute("detalleUser", userService.findById(id_user).get());    	
	return "admin/adminAdminsAdminDetail";
	}
	
	
	/**
	 * Borrar Administrador. Desde Vista de Detalle de Administrador
	 * @param user
	 * @param model
	 * @return
	 */
	@PostMapping("/adminDelete")
	public String adminDelete(@ModelAttribute("detalleUser") UserVO user, Model model) {
		//No se puede eliminar si solo hay un administrador
		if(userService.findByUser_type("ADMIN").size() == 1) {				
			return "errorEliminarAdmin";
		//No se puede Eliminar si es el que está logueado
		}else if(userService.findUserLogged().getId_user() == user.getId_user()){
			return "errorEliminarAutenticado";	
		}else {				
		//Debemos borrar primero el objeto UserRol relacionado
			UserRolVO userRol = userRolService.findByUser(user).get();				
			userRolService.delete(userRol);				
			userService.delete(user);				
			return "redirect:adminList";				
		}
	}
	
	
	/**
	 * Vista Registro nuevo Administrador.Desde Vista de Detalle de Administrador.
	 * @param model
	 * @return
	 */
	@GetMapping("/registerAdmin")
	public String registerAdmin(Model model) {
		model.addAttribute("user", new UserVO());			
		return "admin/registerAdmin";
	}
	
	
	/**
	 * Guardar nuevo Administrador en BBDD.Desde Vista de Detalle de Administrador. Llamada a vista. 
	 * @param user
	 * @param model
	 * @return
	 */
	@PostMapping("/registerAdmin")
	public String registerSuccess(@ModelAttribute UserVO user, Model model){
		//Comprueba que no existe el email ya registrado
		//todo No me gusta, pero funciona. Para mejorar...Llamadas ajax?
				boolean bool;
				try {
				UserVO userWithEmail = userService.findByEmail(user.getEmail()).get();
				bool = true;
				}
				catch(NoSuchElementException e){
				 bool = false;
				}				
				if(bool) {
					return "admin/emailExistsAdmin";
				}
		
		user.setIsActive(true);
		user.setUser_type("ADMIN");
		user.setPassword(userService.encode(user.getPassword()));				
		userService.save(user);
		//Crear Objeto UserRolVO asociado
		UserRolVO userRol = new UserRolVO(0, userService.findById(user.getId_user()).get(),rolService.findById(1).get());
		userRolService.save(userRol);			
		return "admin/signingAdminSuccess";
	}
		
//CLIENTES*************************************************************************
				
	/**
	 * Vista de listado de clientes del área de administración.
	 * @param model
	 * @return
	 */
	@GetMapping ("/clientList")
	public String clientList(Model model){
		//Le pasamos el nombre de usuario autenticado
		model.addAttribute("nick", userService.findUserLogged().getNick());
		//Le pasamos una colección con todos los usuarios Clientes de la BBDD
		model.addAttribute("listaClientes", userService.findByUser_type("CLIENT"));
		return "admin/adminClientsClientList";
	}
		
	/**
	 * vista Actualización de datos de un Cliente
	 * @param detalleUser
	 * @param model
	 * @return
	 */
	@PostMapping("/clientList")
	public String clientUpdate(@ModelAttribute ("detalleUser") UserVO detalleUser, Model model){	
		userService.save(detalleUser);		
		return  "redirect:clientList";
	}
		
	/**
	 * Vista de Detalles de Cliente
	 * @param id_user
	 * @param model
	 * @return
	 */
	@GetMapping ("/clientDetail")		
	public String clientDetail(@RequestParam(name="id_user") int id_user,  Model model){
		//Nick del usuario autenticado
    	model.addAttribute("nick", userService.findUserLogged().getNick());
    	//Le pasamos el UserVO para mostrarlo en formulario de la vista    	
    	model.addAttribute("detalleUser", userService.findById(id_user).get());    	
		return "admin/adminClientsClientDetail";
	}
		
	/**
	 * Borrar Cliente. Desde Vista de Detalle de Cliente
	 * @param user
	 * @param model
	 * @return
	 */
	@PostMapping("/clientDelete")
	public String clientDelete(@ModelAttribute("detalleUser") UserVO user, Model model) {
			//Eliminamos el UserRolVO asociado
			UserRolVO userRol = userRolService.findByUser(user).get();
			userRolService.delete(userRol);			
			//Recogemos en una lista todos los pedidos de ese cliente
			List<PedidoVO> pedidosCliente = pedidoService.findByCliente(user);
			//Recorremos y vamos eliminando cada pedido
			for (PedidoVO pedido : pedidosCliente) {				
				System.out.println("Pedido: " +pedido);				
				pedidoService.delete(pedido);				
			}			
			//Eliminamos el usuario							
			userService.delete(user);				
			return "redirect:clientList";				
		}
	
//RIDERS*************************************************************************
				
	/**
	 * Vista de listado de riders del area de administracion.
	 * @param model
	 * @return
	 */
	@GetMapping ("/riderList")
	public String riderList(Model model){
		//Le pasamos el nombre de usuario
    	model.addAttribute("nick", userService.findUserLogged().getNick());
    	//Le pasamos una colección con todos los usuarios Riders de la BBDD
    	model.addAttribute("listaRiders", userService.findByUser_type("RIDER"));
		return "admin/adminRidersRiderList";
	}
	
	/**
	 * Actualización de datos de un Rider
	 * @param detalleUser
	 * @param model
	 * @return
	 */
	@PostMapping("/riderList")
	public String riderUpdate(@ModelAttribute ("detalleUser") UserVO detalleUser, Model model){				

		userService.save(detalleUser);		
		return  "redirect:riderList";
	}

	/**
	 * Vista de Detalles de Rider
	 * @param id_user
	 * @param model
	 * @return
	 */
	@GetMapping ("/riderDetail")
	public String riderDetail(@RequestParam(name="id_user") int id_user,  Model model){		
		//Nick del usuario autenticado
    	model.addAttribute("nick", userService.findUserLogged().getNick());    	
    	//Le pasamos el UserVO para mostrarlo en formulario de la vista    	
    	model.addAttribute("detalleUser", userService.findById(id_user).get());    	
		return "admin/adminRidersRiderDetail";
	}
		
	/**
	 * Borrar Rider. Desde Vista de Detalle de Rider
	 * @param user
	 * @param model
	 * @return
	 */
	@PostMapping("/riderDelete")
	public String riderDelete(@ModelAttribute("detalleUser") UserVO user, Model model) {
			//Eliminar UserRolVO asociado al Rider
			UserRolVO userRol = userRolService.findByUser(user).get();	
			userRolService.delete(userRol);			
			//Recogemos en una lista todos los pedidos de ese Rider
			List<PedidoVO> pedidosRider = pedidoService.findByRider(user);
			//Recorremos y vamos eliminando cada pedido
			for (PedidoVO pedido : pedidosRider) {				
				System.out.println("Pedido: " +pedido);				
				pedidoService.delete(pedido);		
			}			
			//Eliminamos el Rider							
			userService.delete(userService.findById(user.getId_user()).get());			
			return "redirect:riderList";				
		}
	
//PEDIDOS*************************************************************************
	/**
	 * 
	 */		
	/** 
	 * Vista de listado de Pedidos del area de administracion.
	 * @param model
	 * @return
	 */
	@GetMapping ("/orderList")
	public String orderList(Model model){
		//Le pasamos el nombre de usuario autenticado para mostrar en  el Header
    	model.addAttribute("nick", userService.findUserLogged().getNick());
    	//Le pasamos una colección con todos los pedidos de la BBDD
    	model.addAttribute("listaPedidos", pedidoService.findAll());    	
		return "admin/adminOrdersOrderList";
	}
	
	/**
	 * Actualización de datos de un Pedido
	 * @param detallePedido
	 * @param model
	 * @return
	 */
	@PostMapping("/orderList")
	public String orderUpdate(@ModelAttribute("detallePedido") PedidoVO detallePedido, Model model){
		pedidoService.save(detallePedido);		
//		return "admin/adminOrdersOrderList";
		return  "redirect:orderList";
	}
	
	/**
	 * Vista de Detalles de un Pedido
	 * @param id_pedido
	 * @param model
	 * @return
	 */
	@GetMapping ("/orderDetail")
	public String orderDetail(@RequestParam(name="id_pedido") int id_pedido,  Model model){
		//Le pasamos el nombre de usuario autenticado
    	model.addAttribute("nick", userService.findUserLogged().getNick());    	
    	//Le pasamos el id del pedido para mostrarlo en la cabecera del formulario
    	model.addAttribute("id", id_pedido );
    	//Le pasamos el PedidoVO para mostrarlo en formulario de la vista
    	model.addAttribute("detallePedido", pedidoService.findById(id_pedido));    	
    	//Lista de Clientes para usar un select en el formulario    	
    	model.addAttribute("listaClientes", userService.findByUser_type("CLIENT"));
    	//Lista de Riders para usar un select en el formulario 	
    	model.addAttribute("listaRiders", userService.findByUser_type("RIDER"));    	
		return "admin/adminOrdersOrderDetail";
	}
	
	/**
	 * Borrar Pedido. Desde Vista de Detalle de Pedido
	 * @param pedido
	 * @param model
	 * @return
	 */
	@PostMapping("/orderDelete")
	public String orderDelete(@ModelAttribute("detallePedido") PedidoVO pedido, Model model) {
			pedidoService.delete(pedido);				
			return "redirect:orderList";				
	}	
}
