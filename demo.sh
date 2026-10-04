#!/usr/bin/env bash
set -u

BASE_URL="${BASE_URL:-http://localhost:8080}"

OUT_FILE="${TMPDIR:-/tmp}/subscriptions-demo-body.json"
PASSED=0
FAILED=0

json_id() {
  printf '%s' "$1" | sed -n 's/.*"id"[[:space:]]*:[[:space:]]*\([0-9][0-9]*\).*/\1/p'
}

invoke_api() {
  name="$1"
  method="$2"
  path="$3"
  body="$4"
  expected="$5"

  if [ -n "$body" ]; then
    status=$(curl -sS \
      -H "Accept: application/json" \
      -H "Content-Type: application/json" \
      -o "$OUT_FILE" \
      -w "%{http_code}" \
      -X "$method" \
      --data-binary "$body" \
      "$BASE_URL$path")
  else
    status=$(curl -sS \
      -H "Accept: application/json" \
      -o "$OUT_FILE" \
      -w "%{http_code}" \
      -X "$method" \
      "$BASE_URL$path")
  fi

  response_body=""
  if [ -f "$OUT_FILE" ]; then
    response_body=$(cat "$OUT_FILE")
  fi

  if [ "$status" = "$expected" ]; then
    PASSED=$((PASSED + 1))
    printf 'PASS %s %s %s  %s\n' "$status" "$method" "$path" "$name" >&2
  else
    FAILED=$((FAILED + 1))
    printf 'FAIL %s (expected %s) %s %s  %s\n' "$status" "$expected" "$method" "$path" "$name" >&2
    if [ -n "$response_body" ]; then
      printf '%s\n' "$response_body" >&2
    fi
  fi
}

echo "Demo against $BASE_URL"
echo

invoke_api "list active users" GET "/api/v1/users?page=0&size=10" "" 200
invoke_api "get alice" GET "/api/v1/users/1" "" 200
invoke_api "missing user" GET "/api/v1/users/9999" "" 404

demo_name="demo-$(date +%s)"
invoke_api "create user" POST "/api/v1/users" "{\"username\":\"$demo_name\"}" 201
created_user_id=$(json_id "$(cat "$OUT_FILE")")
invoke_api "duplicate username" POST "/api/v1/users" "{\"username\":\"$demo_name\"}" 400
invoke_api "deactivate new user" PATCH "/api/v1/users/$created_user_id" '{"userAccountStatus":"DEACTIVATED"}' 200
invoke_api "deactivate user who is already deactivated" PATCH "/api/v1/users/$created_user_id" '{"userAccountStatus":"DEACTIVATED"}' 400
invoke_api "list only deactivated users" GET "/api/v1/users?userAccountStatus=DEACTIVATED&size=20" "" 200

invoke_api "list active subscriptions" GET "/api/v1/subscriptions?subscriptionTier=SILVER&subscriptionPlan=MONTHLY" "" 200
invoke_api "get silver monthly" GET "/api/v1/subscriptions/4" "" 200
invoke_api "create gold monthly subscription" POST "/api/v1/subscriptions" '{"subscriptionName":"Demo Gold Monthly","subscriptionTier":"GOLD","subscriptionPlan":"MONTHLY","price":1099}' 201
created_subscription_id=$(json_id "$(cat "$OUT_FILE")")
invoke_api "stop the new subscription" PATCH "/api/v1/subscriptions/$created_subscription_id" '{"subscriptionStatus":"STOPPED","price":999}' 200
invoke_api "reject blank subscription name" POST "/api/v1/subscriptions" '{"subscriptionName":" ","subscriptionTier":"GOLD","subscriptionPlan":"MONTHLY","price":10}' 400

invoke_api "list delivered orders" GET "/api/v1/orders?userId=2" "" 200
invoke_api "get an order" GET "/api/v1/orders/3" "" 200
invoke_api "create order for bob" POST "/api/v1/orders" '{"userId":2,"amount":640}' 201
created_order_id=$(json_id "$(cat "$OUT_FILE")")
invoke_api "mark bob order in transit" PATCH "/api/v1/orders/$created_order_id" '{"orderStatus":"IN_TRANSIT"}' 200
invoke_api "order for missing user" POST "/api/v1/orders" '{"userId":9999,"amount":10}' 404
invoke_api "update order for deactivated dave" PATCH "/api/v1/orders/9" '{"orderStatus":"DELIVERED"}' 400
invoke_api "reject negative amount" POST "/api/v1/orders" '{"userId":1,"amount":-1}' 400

invoke_api "list active memberships" GET "/api/v1/memberships?userId=1" "" 200
invoke_api "get alice membership" GET "/api/v1/memberships/1" "" 200
invoke_api "renew bob membership" PATCH "/api/v1/memberships/2/renew" "" 200
invoke_api "alice is not eligible for silver" PATCH "/api/v1/memberships/1/upgrade" "" 400
invoke_api "alice is already on the lowest tier" PATCH "/api/v1/memberships/1/downGrade" "" 400
invoke_api "bob no longer meets silver rules so downgrade" PATCH "/api/v1/memberships/2/downGrade" "" 200
invoke_api "cancel dave membership that is already cancelled" PATCH "/api/v1/memberships/4/cancel" "" 400
invoke_api "renew a cancelled membership" PATCH "/api/v1/memberships/4/renew" "" 400
invoke_api "cannot join a stopped subscription" POST "/api/v1/memberships" "{\"userId\":2,\"subscriptionId\":$created_subscription_id}" 400
invoke_api "bob joins silver yearly" POST "/api/v1/memberships" '{"userId":2,"subscriptionId":6}' 201

invoke_api "add free delivery to alice free yearly plan" POST "/api/v1/benefits" '{"subscriptionId":3,"benefitType":"FREE_DELIVERY","name":"Free delivery","description":"Free delivery on eligible orders"}' 201
created_benefit_id=$(json_id "$(cat "$OUT_FILE")")
invoke_api "alice receives that benefit" GET "/api/v1/benefits?userId=1&size=20" "" 200
invoke_api "get the benefit" GET "/api/v1/benefits/$created_benefit_id" "" 200
invoke_api "raise the benefit discount" PATCH "/api/v1/benefits/$created_benefit_id" '{"benefitType":"DISCOUNT","discountPercent":10}' 200
invoke_api "reject discount above 100" PATCH "/api/v1/benefits/$created_benefit_id" '{"discountPercent":150}' 400
invoke_api "benefit for missing subscription" POST "/api/v1/benefits" '{"subscriptionId":9999,"benefitType":"PRIORITY_SUPPORT","name":"Priority support"}' 404
invoke_api "benefits require a user id" GET "/api/v1/benefits" "" 400
invoke_api "benefits for a missing user" GET "/api/v1/benefits?userId=9999" "" 404
invoke_api "delete the demo benefit" DELETE "/api/v1/benefits/$created_benefit_id" "" 204
invoke_api "deleted benefit is gone" GET "/api/v1/benefits/$created_benefit_id" "" 404

echo
echo "Passed: $PASSED  Failed: $FAILED"
if [ "$FAILED" -gt 0 ]; then
  exit 1
fi
