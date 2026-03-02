import requests
import json
import time

BASE_URL = 'http://localhost:8081'

def print_res(name, res):
    print(f"[{res.status_code}] {name}")
    if res.status_code >= 400:
        print(f"   -> ERROR: {res.text[:200]}")
    return res

def run_extra_tests():
    print("--- Starting Extra Endpoints Test ---")
    
    # 1. Setup: Register & Login User
    email = f"extra_user_{int(time.time())}@example.com"
    user_payload = {
        "name": "Extra User", "email": email, "password": "password123", "phoneNumber": "1234567890",
        "address": [{"street": "123 Main St", "number": "10A", "neighborhood": "Downtown", "city": "City", "state": "State", "zipCode": "12345", "country": "USA", "type": "SHIPPING", "isDefault": True}]
    }
    res = print_res("Register User", requests.post(f"{BASE_URL}/api/auth/register", json=user_payload))
    if res.status_code != 200: return
    customer_id = res.json().get('id')
    
    res = print_res("Login User", requests.post(f"{BASE_URL}/api/auth/login", json={"email": email, "password": "password123"}))
    token = res.json().get('accessToken')
    headers = {"Authorization": f"Bearer {token}", "Content-Type": "application/json"}
    
    # 2. Address Tests
    address_payload = {"street": "456 Side St", "number": "20B", "neighborhood": "Uptown", "city": "City2", "state": "State2", "zipCode": "54321", "country": "USA", "type": "BILLING", "isDefault": False, "customer": customer_id}
    res = print_res("Add Address", requests.post(f"{BASE_URL}/api/address", json=address_payload, headers=headers))
    if res.status_code == 201:
        address_id = res.json().get('id')
        print_res("Get Addresses", requests.get(f"{BASE_URL}/api/address/customer/{customer_id}", headers=headers))
        update_payload = {"street": "789 Updated St", "number": "30C", "neighborhood": "Suburbs", "city": "City2", "state": "State2", "zipCode": "54321", "country": "USA", "type": "BILLING", "isDefault": False, "customer": customer_id}
        print_res("Update Address", requests.put(f"{BASE_URL}/api/address/{address_id}", json=update_payload, headers=headers))
        print_res("Delete Address", requests.delete(f"{BASE_URL}/api/address/{address_id}", headers=headers))
        
    # 3. Wishlist Tests
    # We need a product ID first. Let's get the first available product.
    products_res = requests.get(f"{BASE_URL}/api/product", headers=headers)
    product_id = None
    if products_res.status_code == 200 and products_res.json().get('content'):
        product_id = products_res.json()['content'][0]['id']
        
    if product_id:
        print_res("Add to Wishlist", requests.post(f"{BASE_URL}/api/wishlist/{product_id}", headers=headers))
        print_res("Get Wishlist", requests.get(f"{BASE_URL}/api/wishlist", headers=headers))
        print_res("Remove from Wishlist", requests.delete(f"{BASE_URL}/api/wishlist/{product_id}", headers=headers))
        
        # 4. Purchase before Review (required by Service)
        print_res("Add to Cart", requests.post(f"{BASE_URL}/api/shopping-cart/{customer_id}", json={"productId": product_id, "quantity": 1}, headers=headers))
        import uuid
        checkout_res = print_res("Checkout", requests.post(f"{BASE_URL}/api/order/{customer_id}", headers={**headers, "Idempotency-Key": str(uuid.uuid4())}))
        order_id = checkout_res.json().get('id')
        
        if order_id:
            # Manually trigger payment success so product can be reviewed
            print_res("Mock Payment Webhook", requests.post(f"{BASE_URL}/api/payment/webhook/success/{order_id}", headers=headers))
            
        print("Waiting for for asynchronous processing...")
        time.sleep(2) 
        
        # 5. Review Tests
        review_payload = {"productId": product_id, "rating": 5, "comment": "Great product!", "title": "Review Title"}
        res = print_res("Add Review", requests.post(f"{BASE_URL}/api/product/{product_id}/review", json=review_payload, headers=headers))
        if res.status_code == 201:
            review_id = res.json().get('id')
            print_res("Get Product Reviews", requests.get(f"{BASE_URL}/api/product/{product_id}/review", headers=headers))
            # Wait for search sync
            time.sleep(2) 
            
    # 5. Search Tests
    print_res("Search Products", requests.get(f"{BASE_URL}/api/search?q=Laptop", headers=headers))
    
    # 6. Promotion Tests
    promo_payload = {"code": f"PROMO{int(time.time())}", "discountType": "PERCENTAGE", "discountValue": 10.0, "startDate": "2026-01-01T00:00:00", "endDate": "2026-12-31T23:59:59", "isActive": True}
    res = print_res("Create Promotion (Admin)", requests.post(f"{BASE_URL}/api/promotion", json=promo_payload, headers=headers))
    if res.status_code == 201:
        print_res("Get Active Promotions", requests.get(f"{BASE_URL}/api/promotion/active", headers=headers))

if __name__ == "__main__":
    run_extra_tests()
