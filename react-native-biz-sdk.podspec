require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |s|
  s.name         = "react-native-biz-sdk"
  s.version      = package["version"]
  s.summary      = package["description"]
  s.description  = <<-DESC
                  react-native-biz-sdk
                   DESC
  s.homepage     = "https://github.com/github_account/react-native-biz-sdk"
  s.license      = "MIT"
  # s.license    = { :type => "MIT", :file => "FILE_LICENSE" }
  s.authors      = { "Your Name" => "yourname@email.com" }
  s.platforms    = { :ios => "13.0" }
  s.source       = { :git => "https://github.com/github_account/react-native-biz-sdk.git", :tag => "#{s.version}" }

  s.source_files = "ios/**/*.{h,m,swift}"
  s.requires_arc = true
  s.resource_bundles = {
    'PayUResource' => ['ios/PayUBizSdkInfo.plist']
  }
  s.dependency "React"
  s.dependency 'PayUIndia-CheckoutPro' , '~> 7.3'
end

