import json
import requests
import time
import uuid
import sys

# Get baseUrl from command line or default to localhost
baseUrl = sys.argv[1] if len(sys.argv) > 1 else "http://localhost:8080"

collection_file = "automated_test_collection.json"
with open(collection_file, 'r') as f:
    collection = json.load(f)

# Initialize variables
variables = {
    "baseUrl": baseUrl,
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
        # It's a folder, run children
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
    
    print(f"--- Running: {name}")
    print(f"{method} {url}")
    
    # Ensure JSON content type
    if body_data and 'Content-Type' not in headers:
        headers['Content-Type'] = 'application/json'

    try:
        if method == "GET":
            res = requests.get(url, headers=headers)
        elif method == "POST":
            res = requests.post(url, headers=headers, data=body_data.encode('utf-8') if body_data else None)
        else:
            print(f"Skipping unsupported method {method}")
            return
            
        print(f"Status: {res.status_code}")
        
        if res.status_code in [200, 201]:
            try:
                res_json = res.json()
                # Update variables for chaining
                if "token" in res_json: variables["jwtToken"] = res_json["token"]
                if name == "Register User": variables["customerId"] = res_json.get("id")
                if name == "Register Vendor": variables["vendorId"] = res_json.get("id")
                if name == "Create Product (Vendor/Admin)": variables["productId"] = res_json.get("id")
                if "orderId" in str(res.url) or "order" in name: 
                    if isinstance(res_json, dict) and "id" in res_json:
                        variables["orderId"] = res_json.get("id")
            except:
                pass
    except Exception as e:
        print(f"Error: {e}")
    print("-" * 30)

# Run the collection
for item in collection.get("item", []):
    run_item(item)
