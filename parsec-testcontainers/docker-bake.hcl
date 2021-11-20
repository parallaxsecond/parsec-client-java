group "default" {
  #targets = ["parsec", "parsec_0.7.0", "parsec_0.8.1"]
  targets = ["parsec_0.8.1", "nginx-test", "greengrass-test"]
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
target "greengrass-test" {
  inherits = ["generic"]
  context = "./greengrass"
  tags = [
    "parallaxsecond/greengrass-test:latest"
  ]
}
target "parsec" {
  inherits = ["generic"]
  context = "./parsec"
  args = {
    PARSEC_BRANCH = "main"
  }
  tags = [
    "parallaxsecond/parsec:latest"
  ]
}
target "parsec_0.8.1" {
  inherits = ["parsec"]
  args = {
    PARSEC_BRANCH = "0.8.1"
  }
  tags = [
    "parallaxsecond/parsec:0.8.1"
  ]
}
target "parsec_0.7.0" {
  inherits = ["parsec"]
  args = {
    PARSEC_BRANCH = "0.7.0"
  }
  tags = [
    "parallaxsecond/parsec:0.7.0"
  ]
}