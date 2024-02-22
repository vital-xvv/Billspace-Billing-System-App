# Application Requirements

## *<p style="text-align: center;">Application User</p>*

1. User new Account(unique email address)
    - Account verification(verify email address)
    - User profile image
    - User details(name, email, job position, bio, phone number, address, etc.)
    - Capability to update user detail information

2. User reset password(without being logged in)
    - Password reset link should expire withing 2 hours

3. User login (using email and password)
    - Token based authentication (JWT Token)
    - Refresh Token seamlessly

4. Brute force attack mitigation (account lock mechanism)
    - Lock account on 6 failed login attempts

5. Role and Permission based application access (ACL)
    - Protect application resources using roles and permissions

6. Two-factor authentication(using user's phone number)
    - Send verification code to user's phone

7. Keep track of users activities (login, account change, etc.)
    - Ability to report suspicious activities
    - Tracking information
        - IP address
        - Device name
        - Browser
        - Date
        - Type
<hr/>        

## *<p style="text-align: center;">Customer</p>*

1. Customer Information
    - Manage customer information(name, address, etc.)
    - Customer can be a person or an institution
    - Customer shpuld have a status
    - Customer will have invoices
    - Print customers in spreadsheet

2. Search Customers
    - Pagination
    - Be able to search customers by name
<hr/>        

## *<p style="text-align: center;">Invoices</p>*

1. Manage Invoices
    - Create new Invoices
    - Add invoices to customers
    - Print invoices declarations for mailing
    - Pring invoices in spreadsheets
    - Download invoices as PDF