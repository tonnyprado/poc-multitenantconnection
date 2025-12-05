**Flujo completo de ejecución del programa**

Este proyecto sigue un flujo muy claro dividido en tres etapas: configuración, resolución de tenant y ejecución de la lógica de negocio. A continuación se describe cómo se ejecuta internamente el programa desde que inicia hasta que atiende una petición.

**1\. Paquete config: inicialización del multitenancy al arrancar la aplicación**

Cuando la aplicación arranca, Spring Boot procesa primero las clases dentro del paquete config. Aquí ocurre lo siguiente:

**a) TenantDataSourceProperties**

- Spring lee el bloque tenants.datasources del archivo application.yml.
- Ese bloque contiene la información de conexión para cada tenant (URL, usuario, contraseña).
- Spring mapea cada entrada a un mapa en memoria: Map&lt;String, SimpleDataSourceProperties&gt;.

**b) DataSourceConfig**

- Con la información anterior, se crea un DataSource independiente por cada tenant.
- Se construye el componente central: TenantRoutingDataSource.
- Este componente recibe todos los DataSource y actúa como un único DataSource para el resto de la aplicación.
- Se registra como el DataSource principal de la aplicación con @Bean.

Resultado de esta fase:  
La aplicación queda preparada con un DataSource dinámico capaz de conectarse a distintas bases de datos según el tenant que esté activo para cada request.

**2\. Paquete tenant: identificación del tenant y control de acceso por request**

Cada vez que llega una petición HTTP, antes de llegar al controlador se ejecuta la cadena de filtros de Spring. En esta etapa interviene el paquete tenant.

**a) TenantFilter**

- Se ejecuta para cada request.
- Extrae los headers:
    - X-Service-Account
    - X-Tenant-ID
- Con esta información llama al resolver para validar si el service account tiene derecho a acceder al tenant solicitado.

**b) ServiceAccountTenantResolver**

- Contiene el mapeo de service accounts hacia sus tenants permitidos.
- Determina si la combinación de service account y tenant es válida.
- Si no es válida devuelve null.
- Si es válida, devuelve el tenant autorizado.

**c) TenantFilter (segunda parte)**

- Si el resolver devuelve null, el filtro rechaza la petición con un código 403.
- Si el resolver devuelve un tenant válido, se llama a:
- TenantContext.setCurrentTenant(tenantId);

Esto almacena el tenant actual en un ThreadLocal, disponible para todo el flujo interno de este request.

Resultado de esta fase:  
El request queda etiquetado con el tenant que le corresponde.  
Si el tenant no es válido, el request no continúa.

**3\. Paquete customer: lógica normal del negocio usando el tenant ya resuelto**

Si el request pasó el filtro, continúa por el flujo estándar de Spring MVC:

**a) CustomerController**

- Atiende la ruta solicitada (GET /api/customers, POST /api/customers).
- Llama a CustomerService.

**b) CustomerService**

- Contiene la lógica de negocio.
- Llama a CustomerRepository.

**c) CustomerRepository (JPA)**

- Ejecuta operaciones como save() o findAll().

**d) Activación del multitenancy en la base de datos**

Cuando JPA necesita una conexión a la base de datos:

- Llama al DataSource global, que es TenantRoutingDataSource.
- TenantRoutingDataSource ejecuta:
- determineCurrentLookupKey()
- Ese método lee el tenant activo desde TenantContext.getCurrentTenant().
- Según ese valor, selecciona el DataSource que corresponde a ese tenant.
- La operación SQL se ejecuta en la base de datos específica de ese tenant.

Resultado de esta fase:  
La consulta o inserción ocurre únicamente dentro de la base de datos asociada al tenant del request, garantizando aislamiento y seguridad.

**Uso del programa en cualquier nube mediante variables de entorno**

Este proyecto es completamente agnóstico a la nube. La aplicación no depende directamente de Azure, AWS o GCP; en cambio, utiliza Spring Boot y JDBC para conectarse a las bases de datos que cada tenant necesita.

Para desplegarlo en cualquier cloud provider, únicamente se requiere:

1.  Subir el artefacto compilado (por ejemplo, el .jar generado por Maven).
2.  Definir las credenciales de cada tenant como **variables de entorno** en la plataforma donde se despliegue.
3.  Spring Boot leerá esas variables y construirá automáticamente los DataSource correspondientes.

La lógica del multitenancy no cambia en absoluto.  
Únicamente cambian las variables de entorno que definen la conexión hacia cada base de datos.

Application.yaml:

```
tenants:
  datasources:
    tenant_a:
      url: ${TENANT_A_DB_URL:jdbc:mysql://localhost:3306/tenant_a_db}
      username: ${TENANT_A_DB_USERNAME:root}
      password: ${TENANT_A_DB_PASSWORD:root}

    tenant_b:
      url: ${TENANT_B_DB_URL:jdbc:mysql://localhost:3306/tenant_b_db}
      username: ${TENANT_B_DB_USERNAME:root}
      password: ${TENANT_B_DB_PASSWORD:root}

```