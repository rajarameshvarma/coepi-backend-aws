output "tcn_base_url" {
  value = "${aws_api_gateway_deployment.tcn_lambda_gateway.invoke_url}/tcnreport"
}
