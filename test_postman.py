import json
import requests
import time
import uuid

collection_file = "automated_test_collection.json"
with open(collection_file, 'r') as f:
    collection = json.load(f)

# Initialize variables
variables = {
    "baseUrl": "http://localhost:8081",
    "userEmail": f"testuser_{int(time.time())}@example.com",
    "jwtToken": "",
    "customerId": "",
    "vendorId": "",
    "productId": "",
    "orderId": "",
    "shipmentId": "",
    "$guid": str(uuid.uuid4()),
    "$timestamp": str(int(time.time()))
}

def replace_vars(text):
    if not isinstance(text, str):
        return text
    for key, value in variables.items():
        text = text.replace(f"{{{{{key}}}}}", str(value))
    return text

def parse_body(body):
    if not body or 'raw' not in body:
        return None
    return replace_vars(body['raw'])

def run_item(item):
    name = item.get("name")
    req = item.get("request")
    if not req:
        for sub in item.get("item", []):
            run_item(sub)
        return

    method = req.get("method")
    url = replace_vars(req.get("url", {}).get("raw"))
    headers = {}
    for h in req.get("header", []):
        headers[h["key"]] = replace_vars(h["value"])
    
    auth = req.get("auth")
    if auth and auth.get("type") == "bearer":
        token = auth["bearer"][0]["value"]
        headers["Authorization"] = f"Bearer {replace_vars(token)}"
        
    body_data = parse_body(req.get("body"))
    
    print(f"==================================================")
    print(f"--- Running: {name}")
    print(f"{method} {url}")
    if body_data:
        # print first 100 chars of body
        print(f"Body: {body_data[:100]}...")
        
    # If body_data is JSON, set Content-Type header
    if body_data and 'Content-Type' not in headers:
        headers['Content-Type'] = 'application/json'

    try:
        if method == "GET":
            res = requests.get(url, headers=headers)
        elif method == "POST":
            res = requests.post(url, headers=headers, data=body_data.encode('utf-8') if body_data else None)
        elif method == "PUT":
            res = requests.put(url, headers=headers, data=body_data.encode('utf-8') if body_data else None)
        elif method == "PATCH":
            res = requests.patch(url, headers=headers, data=body_data.encode('utf-8') if body_data else None)
        elif method == "DELETE":
            res = requests.delete(url, headers=headers)
        else:
            print(f"Unsupported method {method}")
            return
            
        print(f"Status: {res.status_code}")
        try:
            res_json = res.json()
            print(f"Response:")
            print(json.dumps(res_json, indent=2))
            
            # Simple variable extraction based on the collection's tests
            if "token" in res_json:
                variables["jwtToken"] = res_json["token"]
            elif "accessToken" in res_json:
                variables["jwtToken"] = res_json["accessToken"]
            
            if res.status_code in [200, 201]:
                if name == "Register User":
                    variables["customerId"] = res_json.get("id")
                elif name == "Register Vendor":
                    variables["vendorId"] = res_json.get("id")
                elif name == "Create Product (Vendor/Admin)":
                    variables["productId"] = res_json.get("id")
                elif name == "Checkout (Create Order from Cart)":
                    variables["orderId"] = res_json.get("id")
                
        except:
            print(f"Response (text): {res.text}")
    except Exception as e:
        print(f"Error: {e}")
    print()

for item in collection.get("item", []):
    run_item(item)
