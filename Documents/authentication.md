# Authentication and Authorization

## Workflow

### Step 1: JWT Filter (JwtTokenFilter)

1. The client sends an HTTP request to one of your API endpoints with the JWT in the Authorization
   header.
2. The request hits the JwtTokenFilter, which runs before the UsernamePasswordAuthenticationFilter.
3. JwtTokenFilter:

- Extracts the token from the request (usually from the Authorization header).
- Validates the token (e.g., verifies the signature, checks the expiration).
- If the token is valid, it extracts the user details and roles/authorities from the token (usually
  stored in JWT claims).
- An Authentication object (e.g., UsernamePasswordAuthenticationToken) is created with the user
  details and roles, and this is placed into the SecurityContextHolder.
- If the token is invalid or absent, the filter might simply proceed without setting the
  authentication, or it might reject the request based on its logic.
- Once the JwtTokenFilter sets the SecurityContextHolder with a valid Authentication object, Spring Security considers the request authenticated. Subsequent filters in the security chain, including UsernamePasswordAuthenticationFilter, will not perform further authentication because the SecurityContextHolder is already populated with the authenticated user’s details.

### Step 2: Authorization (authorizeHttpRequests)

4. After the JwtTokenFilter sets the Authentication in the SecurityContextHolder, Spring Security’s
   authorization mechanism kicks in.
5. The authorization process (authorizeHttpRequests) checks whether the authenticated user has the
   necessary roles or permissions to access the requested resource:

- For example, if the user tries to access /api/v1/demo/**, the configuration .requestMatchers("
  /api/v1/demo/**").hasRole("ADMIN") checks whether the Authentication object in the
  SecurityContextHolder contains the ROLE_ADMIN authority.
- If the user has the required role, access is granted; otherwise, access is denied.

### Step 3: Authentication Exception Handling

6. If the request is unauthenticated or the token is invalid and the user tries to access a
   protected resource, the HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED) triggers and returns a 401
   Unauthorized response to the client.