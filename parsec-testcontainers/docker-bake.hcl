group "default" {
  targets = ["parsec", "nginx-test"]
}
target "generic" {
  context = "."
  args = {
    REGISTRY = "parallaxsecond"
  }
}
target "nginx-test" {
  inherits = ["generic"]
  context = "./nginx"
  tags = [
    "parallaxsecond/nginx-test:latest"
  ]
}
target "parsec" {
  inherits = ["generic"]
  context = "./parsec"
  tags = [
    "parallaxsecond/parsec:latest"
  ]
}
